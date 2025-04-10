package FeedStudy.StudyFeed.feed.repository;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedReport;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedReportRepository extends JpaRepository<FeedReport, Long> {

    boolean existsByUserAndFeed(User user, Feed feed);

    Optional<FeedReport> findByUserAndFeed(User user, Feed feed);

}
