package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.feed.dto.FeedDetailResponse;
import FeedStudy.StudyFeed.feed.dto.FeedEditRequest;
import FeedStudy.StudyFeed.feed.dto.FeedSimpleDto;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.service.FileService;
import FeedStudy.StudyFeed.global.service.FirebasePublisherService;
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
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

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


    public void create(User user, FeedEditRequest request) {
//        List<FeedImage> images = request.getAddImages().stream()
//                .map(image -> new FeedImage(image.getOriginalFilename()))
//                .toList();
//
//        Feed feed = new Feed();
//        saveFeedImages(feed.getImages(), request.getAddImages());

        Feed feed = new Feed(user, request, new ArrayList<>());
        feedRepository.save(feed);

        feedImageRepository.saveAll(uploadAndCreateImages(request.getAddImages(), feed));

    }

    @Transactional
    public void delete(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateOwner(user, feed);
        deleteImages(feed.getImages());
        feedRepository.delete(feed);

    }

    private List<FeedImage> uploadAndCreateImages(List<MultipartFile> files, Feed feed) {
        return files.stream().map(file -> {
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            fileService.upload(file, fileName);
            String fullUrl = fileService.getFullUrl(fileName);

            FeedImage image = new FeedImage(fullUrl);
            image.initFeed(feed);
            return image;
        }).toList();
    }

    @Transactional
    public void modify(User user, FeedEditRequest request, Long feedId) {
        System.out.println(request);
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        validateOwner(user, feed);
//        Feed.ImageUpdatedResult update = feed.update(request);
//        saveFeedImages(update.getAddedImages(), request.getAddImages());
//        deleteImages(update.getDeletedImages());
        System.out.println(feed.getImages().stream().map(i -> i.getId()).toList());
        List<FeedImage> toDelete = feed.getImages().stream()
                .filter(img -> request.getDeletedImages().contains(img.getId()))
                .toList();
        System.out.println(toDelete);
//        deleteImages(feed.getImages());
//        feed.getImages().clear();
        deleteImages(toDelete);
        feed.getImages().removeAll(toDelete); // 엔티티 연관 제거 (orphanRemoval=true 이므로 DB에서도 삭제됨)


        List<FeedImage> newImages = uploadAndCreateImages(request.getAddImages(), feed);
        feed.getImages().addAll(newImages);
        feed.update(request.getContent(), request.getCategory());
        feedRepository.save(feed);
    }

    public Page<Feed> getMyFeeds(User user, Pageable pageable) {
        return feedRepository.findByUser(user, pageable);
    }

    public Page<Feed> getUserFeeds(User currentUser, Long targetUserId, Pageable pageable) {
        User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new IllegalArgumentException(""));
        List<User> excludedUsers = getExcludedUsers(currentUser);

        if (excludedUsers.contains(targetUser)) {
            throw new MemberException(ErrorCode.BANNED_USER);
        }
        return feedRepository.findByUser(targetUser, pageable);
    }

    public DataResponse getHomeFeeds(User user, Pageable pageable) {
        List<User> excludedUsers = getExcludedUsers(user);
        Page<Feed> feeds;
        if (excludedUsers.isEmpty()) {
            feeds = feedRepository.findAll(pageable);
        } else {
            feeds = feedRepository.findByUserNotIn(excludedUsers, pageable);
        }
        List<FeedSimpleDto> feedDtos = feeds.getContent().stream().map(f -> FeedSimpleDto.toDto(f, user, hasFeedLike(user, f))).toList();
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


    private void deleteImages(List<FeedImage> deletedImages) {
//        deletedImages.forEach(i -> fileService.delete(i.getUniqueName()));

        deletedImages.forEach(image -> {
            String imageUrl = image.getImageUrl();
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            fileService.delete(fileName);
            feedImageRepository.delete(image);
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

    @Transactional
    public FeedDetailResponse getFeedDetail(User user, Long feedId) {
        Feed feed = feedRepository.findByIdWithComments(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateViewPermission(user, feed);

        return FeedDetailResponse.toDto(feed);
    }

    private void validateViewPermission(User currentUser, Feed feed) {
        User feedOwner = feed.getUser();

        boolean isBlocked = blockRepository.existsByBlockerAndBlocked(currentUser, feedOwner)
                || blockRepository.existsByBlockerAndBlocked(feedOwner, currentUser);

        if (isBlocked) {
            throw new MemberException(ErrorCode.BANNED_USER);
        }
    }

//
//    private void saveFeedImages(List<FeedImage> images, List<MultipartFile> addImages) {
//        IntStream.range(0, images.size())
//                .forEach(i -> fileService.upload(addImages.get(i), images.get(i).getUniqueName()));
//    }

}


