package FeedStudy.StudyFeed.repository;

import FeedStudy.StudyFeed.entity.Squad;
import FeedStudy.StudyFeed.entity.SquadMember;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.type.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SquadMemberRepository extends JpaRepository<SquadMember, Long> {


    boolean existsBySquadAndUser(Squad squad, User user);

    long countBySquadAndAttendanceStatus(Squad squad, AttendanceStatus attendanceStatus);

    Optional<SquadMember> findBySquadAndUser(Squad squad, User user);

    boolean existsBySquadAndUserAndAttendanceStatus(Squad squad, User user, AttendanceStatus attendanceStatus);

    List<SquadMember> findAllBySquad(Squad squad);
}
