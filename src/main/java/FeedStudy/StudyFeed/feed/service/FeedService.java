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
        List<FeedImage> images = request.getAddImages().stream()
                .map(image -> new FeedImage(image.getOriginalFilename()))
                .toList();

        Feed feed = new Feed(user, request, images);
        feedRepository.save(feed);

        if(request.getAddImages() != null) {
            saveFeedImages(feed, request.getAddImages());
        }
        return FeedSimpleDto.toDto(feed, user.getId(), false);
    }


    @Transactional
    public FeedDetailResponse getFeed(User user, Long feedId) {
        Feed feed = feedRepository.findByIdWithComments(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        boolean isLike = feedLikeRepository.existsByUserAndFeed(user, feed);
        return FeedDetailResponse.toDto(feed, user.getId(), isLike);
    }


    public FeedRepliesDto getReplies(User user, Long parentId, Pageable pageable) {
        Page<FeedComment> page = feedCommentRepository.findByParentComment_Id(parentId, pageable);
        boolean hasNext = page.hasNext();
        List<FeedCommentDto> replies = page.getContent().stream()
                .map(reply -> FeedCommentDto.toDto(reply, user.getId()))
                .toList();


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

        if(request.getAddImages() != null) {
            saveFeedImages(feed, request.getAddImages());
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
            feedImageRepository.delete(image);
        });
        feedRepository.delete(feed);

    }

    public FeedLikeDto feedLike(Long userId, Long feedId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을수가 없습니다."));
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        boolean isNew = !hasFeedLike(user, feed);

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
        User user = userRepository.findById(userId).orElseThrow(null);
        Page<Feed> feeds = feedRepository.findByUser(user, pageable);
        List<FeedSimpleDto> feedDtos = feeds.getContent().stream()
                .map(f -> FeedSimpleDto.toDto(f, userId, hasFeedLike(user, f))).toList();
        return new FeedResponseDto(feeds.hasNext(), feedDtos);

    }

    public DataResponse getHomeFeeds(User user, Pageable pageable, String category) {
        List<User> excludedUsers = getExcludedUsers(user);
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
        return new DataResponse(feedDtos, hasNext);
    }

    public DataResponse otherFeeds(Long userId, Long otherId, Pageable pageable) {
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Feed> feeds = feedRepository.findByUser(other, pageable);
        List<FeedSimpleDto> feedDtos = feeds.getContent().stream()
                .map(f -> FeedSimpleDto.toDto(f, userId, hasFeedLike(user, f))).toList();

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
      return feedLikeRepository.existsByUserAndFeed(user, feed); // todo 이거 사용자가 이 피드에 대해서 좋아요 버튼을 눌렀는가 아닌가를 확인하는 기능 구현
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
        List<FeedImage> feedImages = images.stream().map(image -> {
            String originalFilename = image.getOriginalFilename();

            FeedImage feedImage = new FeedImage(null, originalFilename);
            s3FileService.upload(image, feedImage.getUniqueName());
            String imgUrl = s3FileService.getFullUrl(feedImage.getUniqueName());

            feedImage.setImageUrl(imgUrl);
            return feedImage;
        }).toList();

        feed.addImage(feedImages);
        feedImageRepository.saveAll(feedImages);
    }


    public void createFeedLike(Feed feed, User user)  {

        FeedLike feedLike = new FeedLike(user, feed);
        feedLikeRepository.save(feedLike);

        String fcmToken = feed.getUser().getFcmToken();
        boolean isAlarm = feed.getUser().getFeedLikeAlarm();
        String title = "게시글 좋아요";
        String content = String.format("회원님의 게시글 [%s]를 좋아합니다.",
                feed.getContent().substring(0, Math.min(20, feed.getContent().length())));
        String data = feed.getId() + ",feed";
        firebaseMessagingService.sendCommentNotification(isAlarm, fcmToken, title, content, data);
    }

    public void decreaseFeedLike(Feed feed, User user) {
        FeedLike feedLike = feedLikeRepository.findByFeedAndUser(feed, user).orElseThrow(null);
        feedLikeRepository.delete(feedLike);
    }


    public void writeComment(Long userId, FeedCommentRequestDto req) {
        User user = userRepository.findById(userId).get();
        Feed feed = feedRepository.findById(req.getFeedId()).get();

        FeedComment parentComment = req.getFeedCommentId() != null
                ? feedCommentRepository.findById(req.getFeedCommentId())
                .orElse(null) : null;

        FeedComment comment = feedCommentRepository.save(new FeedComment(user, feed, req.getComment(), parentComment));
        feed.increaseCommentCount();
        feedRepository.save(feed);

        String fcmToken, pushTitle;
        boolean isAlarm;

        if(req.getFeedCommentId() == null) {
            fcmToken = feed.getUser().getFcmToken();
            isAlarm = feed.getUser().getFeedAlarm();
            pushTitle = "작성하신 글의 새로운 댓글입니다.";
        } else {
            fcmToken = feed.getUser().getFcmToken();
            isAlarm = feed.getUser().getFeedAlarm();
            pushTitle = "작성하신 댓글의 새로운 답글입니다.";
        }
        firebaseMessagingService.sendCommentNotification(isAlarm, fcmToken, pushTitle, req.getComment(), feed.getId() + ",feed");
    }

    public void deleteComment(Long userId, Long commentId) {
        FeedComment comment = feedCommentRepository.
                findByParentComment_IdAndId(commentId, userId).orElseThrow(() ->
                        new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if(!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        if(comment.getParentComment() == null) {
            deleteChildComment(comment);
        }
        feedCommentRepository.delete(comment);
    }

    private void deleteChildComment(FeedComment comment) {
        for (FeedComment child : comment.getChildComments()) {
            deleteChildComment(child);
            feedCommentRepository.delete(child);
        }
    }
}


