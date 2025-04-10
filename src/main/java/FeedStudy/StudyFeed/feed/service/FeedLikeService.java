package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedLike;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;

    public boolean likeClick(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));


        return checkFeedLikes(user, feed);

    }

    private boolean checkFeedLikes(User user, Feed feed) {
        if(feedLikeRepository.existsByUserAndFeed(user, feed)) {
            decrease(user, feed);
            return false;
        } else {
            increase(user, feed);
            return true;
        }
    }

    private void increase(User user, Feed feed) {
        feedLikeRepository.save(new FeedLike(user, feed));
        feed.increaseLikeCount();
    }

    private void decrease(User user, Feed feed) {
        feedLikeRepository.deleteByUserAndFeed(user, feed);
        feed.decreaseLikeCount();
    }


}
