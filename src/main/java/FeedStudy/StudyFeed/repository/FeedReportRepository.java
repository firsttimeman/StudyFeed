package FeedStudy.StudyFeed.repository;

import FeedStudy.StudyFeed.entity.Feed.Feed;
import FeedStudy.StudyFeed.entity.Feed.FeedReport;
import FeedStudy.StudyFeed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedReportRepository extends JpaRepository<FeedReport, Long> {

    boolean existsByUserAndFeed(User user, Feed feed);

    Optional<FeedReport> findByUserAndFeed(User user, Feed feed);

}
