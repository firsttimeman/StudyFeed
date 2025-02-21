package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.dto.FeedEditRequest;
import FeedStudy.StudyFeed.entity.Feed;
import FeedStudy.StudyFeed.entity.FeedImage;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.exception.ErrorCode;
import FeedStudy.StudyFeed.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.repository.BlockRepository;
import FeedStudy.StudyFeed.repository.FeedRepository;
import FeedStudy.StudyFeed.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private FileService fileService;


    public void create(User user, FeedEditRequest request) {
        List<FeedImage> images = request.getAddImages().stream()
                .map(image -> new FeedImage(image.getOriginalFilename()))
                .toList();

        Feed feed = new Feed();
        saveFeedImages(feed.getImages(), request.getAddImages());
    }

    public void modify(User user, FeedEditRequest request, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        validateOwner(user, feed);
        Feed.ImageUpdatedResult update = feed.update(request);
        saveFeedImages(update.getAddedImages(), request.getAddImages());
        deleteImages(update.getDeletedImages());
    }

    public Page<Feed> getMyFeeds(User user, String category, Pageable pageable) {
        return feedRepository.findByUser(user, pageable);
    }

    public Page<Feed> getUserFeeds(User currentUser, Long userId, Pageable pageable) {
        List<User> excludedUsers = getExcludedUsers(currentUser);

        if(excludedUsers.contains(targetUser)) {
            throw new MemberException(ErrorCode.BANNED_USER);
        }
        return feedRepository.findByUser(targetUser, pageable);
    }



    private List<User> getExcludedUsers(User currentUser) {
        List<User> blockedUsers = blockRepository.findByBlocker(currentUser).stream()
                .map(block -> block.getBlocked())
                .toList();

        List<User> blockedByUsers = blockRepository.findByBlocked(currentUser).stream()
                .map(block -> block.getBlocker())
                .toList();

        blockedUsers.addAll(blockedByUsers);
        return blockedUsers.stream().distinct().toList();

    }

    @Transactional
    public void delete(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateOwner(user, feed);
        deleteImages(feed.getImages());
        feedRepository.delete(feed);

    }


    private void deleteImages(List<FeedImage> deletedImages) {
        deletedImages.forEach(i -> fileService.delete(i.getUniqueName()));
    }

    private void validateOwner(User user, Feed feed) {
        if(!user.equals(feed.getUser())) {
            throw new FeedException(ErrorCode.NOT_FEED_USER);
        }
    }


    private void saveFeedImages(List<FeedImage> images, List<MultipartFile> addImages) {
        IntStream.range(0, images.size())
                .forEach(i -> fileService.upload(addImages.get(i), images.get(i).getUniqueName()));
    }
}
