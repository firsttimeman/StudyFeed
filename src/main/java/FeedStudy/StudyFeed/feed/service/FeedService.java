package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.feed.dto.*;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.feed.entity.FeedLike;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.global.config.DistributeLock;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.service.FileService;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.service.FirebasePublisherService;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final FirebasePublisherService firebasePublisherService;
    @Qualifier("s3FileService")
    private final FileService fileService;
    private final FeedLikeRepository feedLikeRepository;
    private final S3FileService s3FileService;
    private final FeedCommentRepository feedCommentRepository;
    private final FirebaseMessagingService firebaseMessagingService;



    @Transactional
    public FeedDetailResponse create(User user, FeedRequest request) {
        // 1) Feed 생성/저장
        Feed feed = feedRepository.save(Feed.create(user, request.getContent(), request.getCategory()));

        // 2) 이미지 업로드/연결
        if (request.getAddedImages() != null && !request.getAddedImages().isEmpty()) {
            saveFeedImages(feed, request.getAddedImages());
        }

        // 3) 방금 생성한 피드 응답
        return FeedDetailResponse.toDto(feed, user.getId(), false);
    }


    @Transactional
    public DataResponse getHomeFeeds(User currentUser, Pageable pageable, Topic category) {
        // 1) 제외할 사용자 ID
        List<Long> excludedIds = getExcludedUsers(currentUser);
        boolean excludedEmpty = excludedIds == null || excludedIds.isEmpty();

        // 2) 카테고리: 전체 → null 로 해석
        Topic categoryFilter = category; // null이면 전체

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

    public FeedRepliesDto getReplies(User me, Long parentId, Pageable pageable) {

        FeedComment parent = feedCommentRepository.findById(parentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        validateViewPermission(me, parent.getFeed());

        Page<FeedComment> page = feedCommentRepository.findReplies(parentId, pageable);

        List<FeedCommentDto> replies = page.getContent().stream()
                .map(r -> FeedCommentDto.forReply(r, parentId, me.getId()))
                .toList();

        int fetched = page.getPageable().getPageNumber() * page.getSize() + page.getNumberOfElements();
        boolean hasMore = parent.getReplyCount() > fetched;

        return new FeedRepliesDto(hasMore, replies);
    }


    //마이페이지에서 나오는것
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




    @Transactional
    public void update(User user, FeedRequest request, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        validateOwner(user, feed);

        // 1) 추가 먼저(내부에서 보상 삭제 처리)
        if (request.getAddedImages() != null && !request.getAddedImages().isEmpty()) {
            saveFeedImages(feed, request.getAddedImages());
        }

        // 2) 본문/카테고리 갱신
        feed.update(request.getContent(), request.getCategory());

        // 3) 삭제는 마지막 (DB 먼저 → afterCommit S3 삭제로 구현해두면 더 안전)
        if (request.getDeletedImages() != null && !request.getDeletedImages().isEmpty()) {
            deleteImages(feed, request.getDeletedImages());
        }
    }


    @Transactional
    public void delete(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        validateOwner(user, feed);

        // 1) S3 키 미리 추출(트랜잭션 안에서 조회)
        List<String> keys = feed.getImages().stream()
                .map(FeedImage::getImageUrl)
                .distinct()
                .map(s3FileService::extractKeyFromUrl)
                .toList();

        // 2) 먼저 DB에서 피드 삭제 (orphanRemoval로 이미지도 같이 삭제)
        feedRepository.delete(feed);

        // 3) 커밋 후 S3 삭제 (베스트 에포트)
        if (!keys.isEmpty() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    for (String key : keys) {
                        try {
                            s3FileService.delete(key);
                        } catch (Exception e) {
                            log.warn("S3 orphan cleanup failed during feed delete. key={}", key, e);
                        }
                    }
                }
            });
        }
    }


    @DistributeLock(keyPrefix = "lock:feed-like:", argIndex = 1)
    @Transactional
    public FeedLikeDto feedLike(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));


        long deleted = feedLikeRepository.deleteByFeedIdAndUserId(feedId, user.getId());
        if (deleted > 0) {
            feed.decreaseLikeCount();
            return FeedLikeDto.toDto(false, feed.getLikeCount());
        }

        FeedLike like = new FeedLike(user, feed);
        feedLikeRepository.save(like);
        feed.increaseLikeCount();

        if (!user.getId().equals(feed.getUser().getId())) {
            sendLikePushAfterCommit(feed);
        }
        return FeedLikeDto.toDto(true, feed.getLikeCount());

    }

    private void sendLikePushAfterCommit(Feed feed) {
    TransactionSynchronizationManager
                .registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendLikePush(feed);
                    }
                });
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
        if (user.getId() != feed.getUser().getId()) {
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


    private void saveFeedImages(Feed feed, List<MultipartFile> images) {
        List<FeedImage> toPersist = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (MultipartFile image : images) {
                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null || !originalFilename.contains(".")) {
                    throw new FeedException(ErrorCode.INVALID_FILE_NAME);
                }

                // 파일 검증(컨텐츠 타입/크기 제한) 추가를 권장
                // e.g. validateMimeSize(image);

                FeedImage feedImage = FeedImage.ofOriginalName(originalFilename); // 팩토리 사용 권장
                String key = feedImage.getUniqueName();

                s3FileService.upload(image, key);
                uploadedKeys.add(key);

                String imgUrl = s3FileService.getFullUrl(key);
                if (imgUrl == null) {
                    throw new FeedException(ErrorCode.IMAGE_URL_GENERATION_FAILED);
                }
                feedImage.setImageUrl(imgUrl);

                toPersist.add(feedImage);
            }

            // 연관만 연결하면 캐스케이드로 저장됨
            feed.addImages(toPersist);

            // 만약 명시 저장을 원하면 아래 한 줄 유지(중복 저장은 아님)
            // feedImageRepository.saveAll(toPersist);

        } catch (Exception e) {
            // 보상 삭제: 이미 업로드 완료된 키들만 제거
            for (String key : uploadedKeys) {
                try {
                    s3FileService.delete(key);
                } catch (Exception deleteEx) {
                    log.warn("Failed to delete uploaded key during compensation: {}", key, deleteEx);
                }
            }
            throw e;
        }
    }


    private void deleteImages(Feed feed, List<String> deletedUrls) {
        List<String> distinct = deletedUrls.stream().distinct().toList();

        // 1) 존재/소유권 검증
        List<FeedImage> targets = feedImageRepository.findAllByImageUrlIn(distinct);
        if (targets.size() != distinct.size()) {
            throw new FeedException(ErrorCode.IMAGE_NOT_FOUND);
        }
        boolean allOwned = targets.stream()
                .allMatch(img -> img.getFeed().getId().equals(feed.getId()));
        if (!allOwned) throw new FeedException(ErrorCode.UNAUTHORIZED_IMAGE_DELETE);

        // 2) S3 키 추출(미리)
        List<String> keys = targets.stream()
                .map(img -> s3FileService.extractKeyFromUrl(img.getImageUrl()))
                .toList();

        // 3) 먼저 DB에서 제거 (트랜잭션 내)
        feedImageRepository.deleteAllInBatch(targets);

        // 4) 커밋 후 S3 삭제 (베스트 에포트)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                // 가능하면 멀티-딜리트 API 사용, 아니면 loop
                for (String key : keys) {
                    try {
                        s3FileService.delete(key);
                    } catch (Exception e) {
                        // 남아도 사용자 화면은 이미 정리된 상태. 운영 로그만 남김
                        log.warn("S3 orphan cleanup failed: {}", key, e);
                    }
                }
            }
        });
    }



    private boolean createFeedLikeSafely(Feed feed, User user)  {

        try {
            feedLikeRepository.save(new FeedLike(user, feed));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }

    }

    private boolean decreaseFeedLikeSafely(Feed feed, User user) {
        return feedLikeRepository.deleteByUserAndFeed(user, feed) > 0;
    }

    private void sendLikePush(Feed feed) {
        String fcmToken = feed.getUser().getFcmToken();
        Boolean isAlarm = feed.getUser().getFeedLikeAlarm();

        if (Boolean.FALSE.equals(isAlarm) || fcmToken == null || fcmToken.isBlank()) {
            return;
        }


        String raw = Optional.ofNullable(feed.getContent()).orElse("(내용 없음)");
        String title = "게시글 좋아요";
        String body = String.format("회원님의 게시글 [%s]를 좋아합니다.",
                raw.substring(0, Math.min(20, raw.length())));
        String data = feed.getId() + ",feed";


        firebaseMessagingService.sendToUser(true, fcmToken, title, body, data);
    }


    @Transactional
    public void writeComment(User user, FeedCommentRequestDto req) {
        // 1) 대상 피드 (feedId로 조회해야 함)
        Feed feed = feedRepository.findById(req.getFeedId())
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        // 2) 조회 권한(차단 관계) 검증
        validateViewPermission(user, feed);

        // 3) 내용 검증
        String raw = Optional.ofNullable(req.getContent()).orElse("").trim();
        if (raw.isEmpty() || raw.length() > 1000) {
            throw new FeedException(ErrorCode.INVALID_CONTENT);
        }

        // 4) 부모 댓글(대댓글) 검증
        FeedComment parent = null;
        if (req.getParentCommentId() != null) {
            parent = feedCommentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

            if (!parent.getFeed().getId().equals(feed.getId())) {
                throw new FeedException(ErrorCode.INVALID_PARENT_COMMENT);
            }
            if (parent.isDeleted()) {
                throw new FeedException(ErrorCode.COMMENT_DELETED_CANNOT_REPLY);
            }
            if (blockRepository.existsByBlockerAndBlocked(user, parent.getUser()) ||
                blockRepository.existsByBlockerAndBlocked(parent.getUser(), user)) {
                throw new FeedException(ErrorCode.BANNED_USER);
            }
        }

        // 5) 저장
        feedCommentRepository.save(new FeedComment(user, feed, raw, parent));

        // 6) 원자 카운트 증감
        feedRepository.increaseCommentCount(feed.getId());
        if (parent != null) {
            feedCommentRepository.increaseReplyCount(parent.getId());
        }

        // 7) 알림 after-commit
        sendCommentPushAfterCommit(user, feed, parent, raw);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        // 1) 대상 댓글 with feed/parent/user
        FeedComment c = feedCommentRepository.findByIdWithFeedAndParentAndUser(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        // 2) 소유자 확인
        if (c.getUser() == null || !c.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.NOT_COMMENT_OWNER);
        }

        boolean hasReplies = c.getReplyCount() > 0;

        if (hasReplies) {
            // 3-a) 대댓글이 있는 루트/답글 → 소프트 삭제
            c.markAsDeleted();
            return;
        }

        // 3-b) 실제 삭제 (대댓글 없음)
        if (c.getParentComment() != null) {
            // 부모 replyCount 감소(원자)
            feedCommentRepository.decreaseReplyCount(c.getParentComment().getId());
        }
        // 피드 commentCount 감소(원자)
        feedRepository.decreaseCommentCount(c.getFeed().getId());

        // 댓글 삭제
        feedCommentRepository.delete(c);
    }




    private void sendCommentPushAfterCommit(User writer, Feed feed, FeedComment parent, String content) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String fcmToken;
                Boolean isAlarm;
                String title;

                if (parent == null) {
                    if (writer.getId().equals(feed.getUser().getId())) return; // 자기 글에 자기 댓글이면 스킵
                    fcmToken = feed.getUser().getFcmToken();
                    isAlarm = feed.getUser().getFeedAlarm();
                    title = "작성하신 글의 새로운 댓글입니다.";
                } else {
                    if (writer.getId().equals(parent.getUser().getId())) return;
                    fcmToken = parent.getUser().getFcmToken();
                    isAlarm = parent.getUser().getFeedAlarm();
                    title = "작성하신 댓글의 새로운 답글입니다.";
                }

                if (Boolean.FALSE.equals(isAlarm) || fcmToken == null || fcmToken.isBlank()) return;

                String body = content.substring(0, Math.min(20, content.length()));
                String data = feed.getId() + ",feed";
                firebaseMessagingService.sendToUser(true, fcmToken, title, body, data);
            }
        });

    }

}





