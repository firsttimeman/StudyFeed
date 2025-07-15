package FeedStudy.StudyFeed.report.repository;

import FeedStudy.StudyFeed.report.entity.ReportUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportUserRepository extends JpaRepository<ReportUser, Long> {

}
