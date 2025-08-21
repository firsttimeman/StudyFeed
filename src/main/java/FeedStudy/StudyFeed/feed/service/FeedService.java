package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.feed.dto.*;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.feed.entity.FeedLike;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.service.FileService;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.service.FirebasePublisherService;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
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


    public FeedSimpleDto create(User user, FeedRequest request) {
        // 1. 피드 생성 (이미지는 일단 빈 리스트로 생성)
        Feed feed = new Feed(user, request, new ArrayList<>());
        feedRepository.save(feed);

        // 2. 이미지가 있으면 업로드 및 연결
        if (request.getAddedImages() != null && !request.getAddedImages().isEmpty()) {
            saveFeedImages(feed, request.getAddedImages());
        }

        // 3. 결과 반환
        return FeedSimpleDto.toDto(feed, user.getId(), false);
    }


    @Transactional
    public FeedDetailResponse getFeed(User user, Long feedId) {
        Feed feed = feedRepository.findByIdWithComments(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        boolean isLike = feedLikeRepository.existsByUserAndFeed(user, feed);
        return FeedDetailResponse.toDto(feed, user.getId(), isLike); //todo  떄문에 N+1 문제 발생
        //댓글 dto에서 사용자/대댓글 접근 문제 때문에 N+1문제 발생
    }


    public FeedRepliesDto getReplies(User user, Long parentId, Pageable pageable) {
        Page<FeedComment> page = feedCommentRepository.findByParentComment_Id(parentId, pageable);
        boolean hasNext = page.hasNext();
        List<FeedCommentDto> replies = page.getContent().stream()
                .map(reply -> FeedCommentDto.toDto(reply, user.getId())) // todo n+1 발생 가능
                .toList();
        //댓글·대댓글·작성자 지연로딩 연쇄 N+1.

        return new FeedRepliesDto(hasNext, replies);
    }

    @Transactional
    public void update(User user, FeedRequest request, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        validateOwner(user, feed);

        if(request.getDeletedImages() != null) {
            deleteImages(request.getDeletedImages());
        }

        if(request.getAddedImages() != null) {
            saveFeedImages(feed, request.getAddedImages());
        }


        feed.update(request.getContent(), request.getCategory());
        feedRepository.save(feed);
    }


    @Transactional
    public void delete(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateOwner(user, feed);
        List<FeedImage> images = feedImageRepository.findByFeed(feed);
        images.forEach(image -> {
            s3FileService.delete(image.getImageUrl());
            feedImageRepository.delete(image); // todo n+1 문제가 안생긴다고는 하는데 의심이 됨
        });
        feedRepository.delete(feed);

    }

    @Transactional
    public FeedLikeDto feedLike(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        boolean isNew = !hasFeedLike(user, feed);
        System.out.println("isNew = " + isNew);
        if(isNew) {
            feed.increaseLikeCount();
            createFeedLike(feed, user);
        } else {
            feed.decreaseLikeCount();
            decreaseFeedLike(feed, user);
        }
        feedRepository.save(feed);
        return FeedLikeDto.toDto(isNew, feed.getLikeCount());
    }

//    private List<FeedImage> uploadAndCreateImages(List<MultipartFile> files, Feed feed) {
//        return files.stream().map(file -> {
//            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
//            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
//
//            fileService.upload(file, fileName);
//            String fullUrl = fileService.getFullUrl(fileName);
//
//            FeedImage image = new FeedImage(fullUrl);
//            image.initFeed(feed);
//            return image;
//        }).toList();
//    }


    public FeedResponseDto myFeeds (Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        Page<Feed> feeds = feedRepository.findByUser(user, pageable);
        List<FeedSimpleDto> feedDtos = feeds.getContent().stream()
                .map(f -> FeedSimpleDto.toDto(f, userId, hasFeedLike(user, f))).toList(); // todo N + 1 문제 예상
        //f.getUser().getNickName();   // Feed.user LAZY → 피드마다 1회
        //f.getUser().getImageUrl();   // (동일 User 로딩 후엔 추가 쿼리 없음)
        //f.getImages()...             // Feed.images LAZY 컬렉션 → 피드마다 1회
        // // existsByUserAndFeed(user, f)
        //→ 피드마다 EXISTS 쿼리 1회 추가.
        //결국 메인 1 + (User N) + (Images N) + (LikeExists N) = 1 + 3N 수준의 쿼리로 불어납니다.
        //(실제 수치는 캐시/동일 사용자 겹침 등에 따라 약간 달라질 수 있지만 패턴은 위와 같아요.)
        // 이중 n+1 발생 가능

        return new FeedResponseDto(feeds.hasNext(), feedDtos);

    }

    public DataResponse getHomeFeeds(User user, Pageable pageable, String category) {
        List<User> excludedUsers = getExcludedUsers(user); // todo n+1 발생 가능
        //	•	Block.blocked, Block.blocker가 LAZY면 블록 레코드 수만큼 추가 SELECT → N+1(α)

        Page<Feed> feeds;
        if (!excludedUsers.isEmpty()) {
            feeds = category.equals("전체")
                    ? feedRepository.findByUserNotIn(excludedUsers, pageable) :
                    feedRepository.findByCategoryAndUserNotIn(category, excludedUsers, pageable);
        } else {
            feeds = category.equals("전체")
                    ? feedRepository.findAll(pageable)
                    : feedRepository.findAllByCategory(category, pageable);
        }
        boolean hasNext = feeds.hasNext();
        List<FeedSimpleDto> feedDtos = feeds.getContent().stream().map(f -> FeedSimpleDto.toDto(f, user.getId(), hasFeedLike(user, f))).toList();
        // 여기서 n+1 발생 피드마다 존재여부 확인 toDto로 또 한번의 n+1 발생
        //	•	FeedSimpleDto.toDto 안에서:
        //	•	f.getUser().getNickName()/getImageUrl → Feed.user LAZY 피드마다 1회
        //	•	f.getImages() → Feed.images 컬렉션 LAZY 피드마다 1회
        //→ N+1
        //	•	hasFeedLike(user, f):
        //	•	existsByUserAndFeed를 피드마다 1회 → 또 N+1
        return new DataResponse(feedDtos, hasNext);
    }

    public DataResponse otherFeeds(Long userId, Long otherId, Pageable pageable) {
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        Page<Feed> feeds = feedRepository.findByUser(other, pageable);
        List<FeedSimpleDto> feedDtos = feeds.getContent().stream()
                .map(f -> FeedSimpleDto.toDto(f, userId, hasFeedLike(user, f)))
                .toList();//todo n+1 발생

        //1.	DTO 변환 중 LAZY 접근 (N+1)
        //	•	FeedSimpleDto.toDto 내부에서
        //	•	f.getUser().getNickName()/getImageUrl → Feed.user LAZY → 피드마다 추가 SELECT
        //	•	f.getImages() → Feed.images LAZY 컬렉션 → 피드마다 추가 SELECT
        //	2.	피드별 좋아요 존재 조회 (N+1)
        //	•	hasFeedLike(user, f) → existsByUserAndFeed(user, f)를 피드마다 1회 실행
        //
        //즉, 메인 페이징 쿼리(+count) 이후에
        //	•	작성자 로딩 N회 + 이미지 로딩 N회 + 좋아요 존재 확인 N회가 더해져 이중 N+1 패턴이 됩니다.
        //
        //위쪽의 userRepository.findById(...) 두 번은 단건 조회라 N+1과 무관합니다.

        return new DataResponse(feedDtos, feeds.hasNext());

    }




    private List<User> getExcludedUsers(User currentUser) {
        List<User> blockedUsers = new ArrayList<>(blockRepository.findByBlocker(currentUser).stream()
                .map(block -> block.getBlocked())
                .toList());

        List<User> blockedByUsers = blockRepository.findByBlocked(currentUser).stream()
                .map(block -> block.getBlocker())
                .toList();

        blockedUsers.addAll(blockedByUsers);
        return blockedUsers.stream().distinct().toList();

    }


    private void deleteImages(List<String> deletedImages) {

        deletedImages.forEach(imageUrl -> {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/" ) + 1);
            s3FileService.delete(fileName);
            feedImageRepository.deleteByImageUrl(imageUrl);
        });


    }

    public boolean hasFeedLike(User user, Feed feed) {
      return feedLikeRepository.existsByUserAndFeed(user, feed);
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
//        List<FeedImage> feedImages = images.stream().map(image -> {
//            String originalFilename = image.getOriginalFilename();
//
//            FeedImage feedImage = new FeedImage(null, originalFilename);
//            s3FileService.upload(image, feedImage.getUniqueName());
//            String imgUrl = s3FileService.getFullUrl(feedImage.getUniqueName());
//
//            if (imgUrl == null) {
//                throw new IllegalStateException("이미지 URL이 null입니다. S3 업로드 또는 URL 생성에 실패했습니다.");
//            }
//
//            feedImage.setImageUrl(imgUrl);
//            return feedImage;
//        }).toList();
//
//        feed.addImage(feedImages);
//        feedImageRepository.saveAll(feedImages);

        List<FeedImage> feedImages = images.stream()
                .map(image -> {
                    String originalFilename = image.getOriginalFilename();

                    // 안정성 검사
                    if (originalFilename == null || !originalFilename.contains(".")) {
                        throw new FeedException(ErrorCode.INVALID_FILE_NAME);
                    }

                    // FeedImage 생성 및 uniqueName 생성
                    FeedImage feedImage = new FeedImage(null, originalFilename);

                    // S3 업로드
                    s3FileService.upload(image, feedImage.getUniqueName());

                    // URL 생성
                    String imgUrl = s3FileService.getFullUrl(feedImage.getUniqueName());
                    if (imgUrl == null) {
                        throw new FeedException(ErrorCode.IMAGE_URL_GENERATION_FAILED);
                    }

                    // URL 세팅
                    feedImage.setImageUrl(imgUrl);
                    return feedImage;
                })
                .toList();

        // 연관관계 세팅
        feed.addImage(feedImages);

        // DB 저장
        feedImageRepository.saveAll(feedImages);


    }


    private void createFeedLike(Feed feed, User user)  {
        FeedLike feedLike = new FeedLike(user, feed);
        feedLikeRepository.save(feedLike);
        String fcmToken = feed.getUser().getFcmToken();
        Boolean isAlarm = feed.getUser().getFeedLikeAlarm();

        String title = "게시글 좋아요";
        String content = String.format("회원님의 게시글 [%s]를 좋아합니다.",
                feed.getContent().substring(0, Math.min(20, feed.getContent().length())));
        String data = feed.getId() + ",feed";
        firebaseMessagingService.sendCommentNotification(isAlarm, fcmToken, title, content, data);
    }

    private void decreaseFeedLike(Feed feed, User user) {
        FeedLike feedLike = feedLikeRepository.findByFeedAndUser(feed, user).orElseThrow(null);
        feedLikeRepository.delete(feedLike);
    }


    public void writeComment(User user, FeedCommentRequestDto req) {
        System.out.println(req);
        Feed feed = feedRepository.findById(req.getFeedPid())
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        FeedComment parentComment = req.getFeedCommentPid() != null
                ? feedCommentRepository.findById(req.getFeedCommentPid())
                .orElse(null) : null;

        feedCommentRepository.save(new FeedComment(user, feed, req.getContent(), parentComment));
        feed.increaseCommentCount();
        feedRepository.save(feed);

        String fcmToken;
        Boolean isAlarm;
        String pushTitle;
        String pushContent = req.getContent();
        String data = feed.getId() + ",feed";

        if (parentComment == null) {
            fcmToken = feed.getUser().getFcmToken();
            isAlarm = feed.getUser().getFeedAlarm();
            pushTitle = "작성하신 글의 새로운 댓글입니다.";
        } else {
            fcmToken = parentComment.getUser().getFcmToken();
            isAlarm = parentComment.getUser().getFeedAlarm();
            pushTitle = "작성하신 댓글의 새로운 답글입니다.";
        }

        firebaseMessagingService.sendCommentNotification(isAlarm, fcmToken, pushTitle, pushContent, data);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.NOT_COMMENT_OWNER);
        }

       comment.markAsDeleted();
    }

    private void deleteChildComment(FeedComment comment) {
        for (FeedComment child : comment.getChildComments()) {
            deleteChildComment(child);
            feedCommentRepository.delete(child);
        }
    }

    /*
    •	DTO들
	•	feed/dto/FeedSimpleDto : feed.getUser().getNickName(), feed.getImages()...
	•	feed/dto/FeedDetailResponse : feed.getImages(), feed.getComments()...
	•	feed/dto/FeedCommentDto : comment.getChildComments(), comment.getUser()
→ 목록/상세 전반에서 지연로딩 접근.
     */
}


