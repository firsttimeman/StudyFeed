package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.entity.Feed;
import FeedStudy.StudyFeed.entity.FeedLike;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.exception.ErrorCode;
import FeedStudy.StudyFeed.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;

    public boolean LikeClick(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        if(feedLikeRepository.existsByUserAndFeed(user, feed)) {
            feedLikeRepository.deleteByUserAndFeed(user, feed);
            feed.decreaseLikeCount();
            return false;
        } else {
            feedLikeRepository.save(new FeedLike(user, feed));
            feed.increaseLikeCount();
            return true;
        }

    }


}
