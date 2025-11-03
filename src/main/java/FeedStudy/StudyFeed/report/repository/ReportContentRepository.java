package FeedStudy.StudyFeed.report.repository;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.report.entity.ReportContent;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportContentRepository extends JpaRepository<ReportContent, Long> {
    boolean existsByReporterAndFeed(User reporter, Feed feed);
    boolean existsByReporterAndSquad(User reporter, Squad squad);

    long countByFeed(Feed feed);
    long countBySquad(Squad squad);
}