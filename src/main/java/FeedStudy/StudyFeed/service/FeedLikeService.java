package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.entity.Feed.Feed;
import FeedStudy.StudyFeed.entity.Feed.FeedLike;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.exception.ErrorCode;
import FeedStudy.StudyFeed.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.repository.FeedRepository;
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
