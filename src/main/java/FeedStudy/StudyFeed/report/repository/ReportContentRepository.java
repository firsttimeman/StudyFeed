package FeedStudy.StudyFeed.report.repository;

import FeedStudy.StudyFeed.report.entity.ReportContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportContentRepository extends JpaRepository<ReportContent, Long> {
}
