package FeedStudy.StudyFeed.repository;

import FeedStudy.StudyFeed.entity.Feed;
import FeedStudy.StudyFeed.entity.FeedLike;
import FeedStudy.StudyFeed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    boolean existsByUserAndFeed(User user, Feed feed);

    void deleteByUserAndFeed(User user, Feed feed);
}
