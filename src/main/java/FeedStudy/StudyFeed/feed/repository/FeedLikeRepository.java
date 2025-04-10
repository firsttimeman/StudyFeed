package FeedStudy.StudyFeed.feed.repository;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedLike;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    boolean existsByUserAndFeed(User user, Feed feed);

    void deleteByUserAndFeed(User user, Feed feed);
}
