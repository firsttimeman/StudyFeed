package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.feed.dto.*;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static FeedStudy.StudyFeed.feed.dto.FeedCommentDto.fmt;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final S3FileService s3FileService;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;

    @Transactional
    public FeedDetailResponse create(User user, FeedRequest request) { //todo feedRequest 분리 고려
        Feed feed = feedRepository.save(Feed.create(user, request.getContent(), request.getCategory()));

        if (request.getAddedImagesUrls() != null && !request.getAddedImagesUrls().isEmpty()) {
            saveFeedImages(feed, request.getAddedImagesUrls());
        }

        return FeedDetailResponse.toDto(feed, user.getId(), false);
    }


    @Transactional
    public void delete(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        validateOwner(user, feed);

        // 1) 트랜잭션 안에서 이미지 URL들만 추출
        List<String> imageUrls = feed.getImages().stream()
                .map(FeedImage::getImageUrl)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2) 먼저 DB에서 피드 삭제 (orphanRemoval로 feed_image도 같이 삭제)
        feedRepository.delete(feed);

        // 3) 커밋 후 S3 삭제 (베스트 에포트 + S3 전용 헬퍼 사용)
        if (!imageUrls.isEmpty() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteImagesInS3ByUrls(imageUrls);
                }
            });
        }
    }

    @Transactional
    public void update(User user, FeedRequest request, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateOwner(user, feed);

        feed.update(request.getContent(), request.getCategory());

        if (request.getAddedImagesUrls() != null && !request.getAddedImagesUrls().isEmpty()) {
            saveFeedImages(feed, request.getAddedImagesUrls());
        }

        if (request.getDeletedImagesUrls() != null && !request.getDeletedImagesUrls().isEmpty()) {
            List<String> urlsToDelete = deleteImagesFromDb(feed, request.getDeletedImagesUrls());

            if (!urlsToDelete.isEmpty() && TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deleteImagesInS3ByUrls(urlsToDelete);
                    }
                });
            }
        }
    }


    @Transactional
    public DataResponse getHomeFeeds(User currentUser, Pageable pageable, Topic category) {
        // 1) 제외할 사용자 ID
        List<Long> excludedIds = getExcludedUsers(currentUser);
        boolean excludedEmpty = excludedIds.isEmpty();

        // 2) 카테고리: 전체 → null 로 해석
        Topic categoryFilter = category;

        // 3) 조회 (user join fetch via EntityGraph)
        Page<Feed> page = feedRepository.findHomeFeeds(
                categoryFilter,
                excludedEmpty,
                excludedEmpty ? List.of() : excludedIds,
                pageable
        );

        List<Feed> feeds = page.getContent();
        if (feeds.isEmpty()) return new DataResponse(List.of(), false);

        // 4) 이미지, 좋아요 상태 batch 조회
        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();

        List<ImageRow> rows = feedImageRepository.findPairsByFeedIdIn(feedIds); // 내부에서 order by 보장 권장
        Map<Long, List<String>> imagesByFeed = rows.stream()
                .collect(Collectors.groupingBy(
                        ImageRow::feedId,
                        LinkedHashMap::new,
                        Collectors.mapping(ImageRow::imageUrl, Collectors.toList())
                ));

        Set<Long> likedIdSet = new HashSet<>(feedLikeRepository.findLikedFeedIds(currentUser, feedIds));

        // 5) DTO 매핑
        List<FeedSimpleDto> list = feeds.stream()
                .map(f -> FeedSimpleDto.of(
                        f,
                        imagesByFeed.getOrDefault(f.getId(), List.of()),
                        likedIdSet.contains(f.getId()),
                        currentUser.getId()
                ))
                .toList();

        return new DataResponse(list, page.hasNext());
    }


    @Transactional
    public FeedDetailDto getFeedDetail(User me, Long feedId, Pageable rootPageable, int previewLimit) {

        Feed feed = feedRepository.findDetailForView(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateViewPermission(me, feed);

        boolean likedByMe = feedLikeRepository.existsByUserAndFeed(me, feed);

        List<String> images = feedImageRepository.findByFeedIdOrderByIdAsc(feedId)
                .stream().map(FeedImage::getImageUrl).toList();

        Page<FeedComment> roots = feedCommentRepository.findRootComments(feedId, rootPageable);
        boolean hasMoreComments = roots.hasNext();

        List<FeedCommentDto> rootDtos = roots.getContent().stream().map(root -> {
            var topN = feedCommentRepository
                    .findTopByParentCommentIdOrderByIdAsc(root.getId(), Pageable.ofSize(previewLimit))
                    .stream()
                    .map(r -> FeedCommentDto.forReply(r, root.getId(), me.getId()))
                    .toList();

            return FeedCommentDto.forRoot(root, topN, me.getId());
        }).toList();

        return new FeedDetailDto(
                feed.getId(),
                feed.getUser().getNickName(),
                feed.getUser().getImageUrl(),
                feed.getCategory(),
                feed.getContent(),
                images,
                likedByMe,
                feed.getLikeCount(),
                feed.getCommentCount(),
                feed.getUser().getId().equals(me.getId()),
                fmt(feed.getCreatedAt()),
                hasMoreComments,
                rootDtos
        );
    }


    @Transactional
    public DataResponse getMyFeeds (User me, Pageable pageable) {
        Page<Feed> page = feedRepository.findMyFeeds(me.getId(), pageable);

        List<Feed> feeds = page.getContent();
        if(feeds.isEmpty()) return new DataResponse(List.of(), false);

        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();

        List<ImageRow> rows = feedImageRepository.findPairsByFeedIdIn(feedIds);
        Map<Long, List<String>> imagesByFeed = rows.stream()
                .collect(Collectors.groupingBy(
                        ImageRow::feedId,
                        LinkedHashMap::new,
                        Collectors.mapping(ImageRow::imageUrl, Collectors.toList())
                ));

        Set<Long> likedIdSet = new HashSet<>(
                feedLikeRepository.findLikedFeedIds(me, feedIds)
        );


        List<FeedSimpleDto> list = feeds.stream()
                .map(f -> FeedSimpleDto.of(
                        f,
                        imagesByFeed.getOrDefault(f.getId(), List.of()),
                        likedIdSet.contains(f.getId()),
                        me.getId()
                ))
                .toList();

        return new DataResponse(list, page.hasNext());
    }


    //todo 겹치는 로직 추후에 따로 뺴기
    public DataResponse getUserFeeds(User me, Long otherUserId, Pageable pageable) {

        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));


        boolean blocked =
                blockRepository.existsByBlockerAndBlocked(me, other) ||
                blockRepository.existsByBlockerAndBlocked(other, me);
        if (blocked) {
            throw new MemberException(ErrorCode.BANNED_USER);
        }


        Page<Feed> page = feedRepository.findMyFeeds(otherUserId, pageable);

        List<Feed> feeds = page.getContent();
        if (feeds.isEmpty()) {
            return new DataResponse(List.of(), false);
        }


        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();
        List<ImageRow> rows = feedImageRepository.findPairsByFeedIdIn(feedIds);

        Map<Long, List<String>> imagesByFeed = rows.stream()
                .collect(Collectors.groupingBy(
                        ImageRow::feedId,
                        LinkedHashMap::new,
                        Collectors.mapping(ImageRow::imageUrl, Collectors.toList())
                ));


        Set<Long> likedIdSet = new HashSet<>(feedLikeRepository.findLikedFeedIds(me, feedIds));


        List<FeedSimpleDto> list = feeds.stream()
                .map(f -> FeedSimpleDto.of(
                        f,
                        imagesByFeed.getOrDefault(f.getId(), List.of()),
                        likedIdSet.contains(f.getId()),
                        me.getId()
                ))
                .toList();

        return new DataResponse(list, page.hasNext());
    }


    //todo 업로드 되고 실제 피드에 연결 안된 s3 이미지 정책 생각하기
    public List<String> uploadImagesInS3(Long userId, List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            String original = Optional.ofNullable(file.getOriginalFilename()).orElse("image");
            String ext = "";

            int dotIdx = original.lastIndexOf('.');
            if (dotIdx != -1) {
                ext = original.substring(dotIdx); // ".png" 같은 확장자
            }

            String key = String.format(
                    "feed/temp/%d/%s%s",
                    userId,
                    UUID.randomUUID(),
                    ext
            );

            String url = s3FileService.uploadAndReturnUrl(file, key);
            urls.add(url);
        }

        return urls;
    }


    private Map<String, Boolean> deleteImagesInS3ByUrls(List<String> urls) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        if (urls == null || urls.isEmpty()) return result;

        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            try {
                s3FileService.deleteByUrl(url);
                result.put(url, true);
            } catch (Exception e) {
                log.warn("S3 이미지 삭제 실패. url={}", url, e);
                result.put(url, false);
            }
        }
        return result;
    }

    private List<String> deleteImagesFromDb(Feed feed, List<String> deletedUrls) {

        List<String> distinct = deletedUrls.stream()
                .filter(Objects::nonNull)
                .filter(url -> !url.isBlank())
                .distinct()
                .toList();

        if(distinct.isEmpty()) return List.of();

        List<FeedImage> targets = feedImageRepository.findAllByImageUrlInWithFeed(distinct);
        if(targets.size() != distinct.size()) {
            throw new FeedException(ErrorCode.IMAGE_NOT_FOUND);
        }

        boolean allOwned = targets.stream()
                .allMatch(img -> img.getFeed().getId().equals(feed.getId()));
        if (!allOwned) {
            throw new FeedException(ErrorCode.UNAUTHORIZED_IMAGE_DELETE);
        }

        feedImageRepository.deleteAllInBatch(targets);

        return targets.stream()
                .map(FeedImage::getImageUrl)
                .toList();

    }





    private List<Long> getExcludedUsers(User currentUser) {
        Set<Long> ids = new HashSet<>();

        blockRepository.findByBlockerWithBlocked(currentUser)
                .forEach(b -> ids.add(b.getBlocked().getId()));

        blockRepository.findByBlockedWithBlocker(currentUser)
                .forEach(b -> ids.add(b.getBlocker().getId()));

        return new ArrayList<>(ids);
    }


    private void validateOwner(User user, Feed feed) {
        if (!user.getId().equals(feed.getUser().getId())) {
            throw new FeedException(ErrorCode.NOT_FEED_USER);
        }
    }



    private void validateViewPermission(User currentUser, Feed feed) {
        User feedOwner = feed.getUser();

        boolean isBlocked = blockRepository.existsByBlockerAndBlocked(currentUser, feedOwner)
                || blockRepository.existsByBlockerAndBlocked(feedOwner, currentUser);

        if (isBlocked) {
            throw new MemberException(ErrorCode.BANNED_USER);
        }
    }


    private void saveFeedImages(Feed feed, List<String> imagesUrls) {

        List<FeedImage> images = imagesUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .distinct()
                .map(url -> {
                    FeedImage feedImage = FeedImage.fromUrl(url);
                    feedImage.initFeed(feed);
                    return feedImage;
                })
                .toList();

        feed.addImages(images);


    }









}





