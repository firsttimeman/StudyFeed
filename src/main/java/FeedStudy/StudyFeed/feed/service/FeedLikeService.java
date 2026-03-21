package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.feed.dto.FeedLikeDto;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedLike;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.config.DistributeLock;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final BlockRepository blockRepository;

    @Transactional
    @DistributeLock(keyPrefix = "lock:feed-like:", argIndex = 1)
    public FeedLikeDto toggleLike(User user, Long feedId) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateViewPermission(user, feed);

        long deleted = feedLikeRepository.deleteByFeedIdAndUserId(feedId, user.getId());

        if (deleted > 0) {
            feedLikeRepository.decreaseLikeCount(feedId);
            return FeedLikeDto.toDto(false);
        }

        FeedLike like = new FeedLike(user, feed);
        feedLikeRepository.save(like);

        feedLikeRepository.incrementLikeCount(feedId);

        return FeedLikeDto.toDto(true);
    }


    private void validateViewPermission(User currentUser, Feed feed) {
        User feedOwner = feed.getUser();

        boolean isBlocked = blockRepository.existsByBlockerAndBlocked(currentUser, feedOwner)
                            || blockRepository.existsByBlockerAndBlocked(feedOwner, currentUser);

        if (isBlocked) {
            throw new MemberException(ErrorCode.BANNED_USER);
        }
    }




}
