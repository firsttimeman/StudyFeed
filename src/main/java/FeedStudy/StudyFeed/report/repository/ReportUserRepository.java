package FeedStudy.StudyFeed.report.repository;

import FeedStudy.StudyFeed.report.entity.ReportUser;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportUserRepository extends JpaRepository<ReportUser, Long> {
    boolean existsByReporterAndReported(User reporter, User reported);
    long countByReported(User reported);

    @EntityGraph(attributePaths = {"reported"})
    Page<ReportUser> findByReporter(User reporter, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter"})
    Page<ReportUser> findByReported(User reported, Pageable pageable);
}