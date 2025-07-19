package FeedStudy.StudyFeed.squad.repository;

import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SquadMemberRepository extends JpaRepository<SquadMember, Long> {


    boolean existsBySquadAndUser(Squad squad, User user);

    long countBySquadAndAttendanceStatus(Squad squad, AttendanceStatus attendanceStatus);

    Optional<SquadMember> findBySquadAndUser(Squad squad, User user);

    boolean existsBySquadAndUserAndAttendanceStatus(Squad squad, User user, AttendanceStatus attendanceStatus);

    List<SquadMember> findAllBySquad(Squad squad);

    List<SquadMember> findByUser(User user);
}
