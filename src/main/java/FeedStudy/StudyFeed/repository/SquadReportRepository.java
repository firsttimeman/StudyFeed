package FeedStudy.StudyFeed.repository;

import FeedStudy.StudyFeed.entity.Squad;
import FeedStudy.StudyFeed.entity.SquadReport;
import FeedStudy.StudyFeed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SquadReportRepository extends JpaRepository<SquadReport, Long> {

    boolean existsByReporterAndReportedUserAndSquad(User reporter, User reportedUser, Squad squad);

    long countBySquadAndReportedUser(Squad squad, User reportedUser);

}
