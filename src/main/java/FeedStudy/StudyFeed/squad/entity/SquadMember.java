package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.SquadAccessType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SquadMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id", nullable = false)
    private Squad squad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus attendanceStatus;

    private boolean chatEntered = false;


    public static SquadMember createSquadMember(Squad squad, User user) {
        return SquadMember.builder()
                .squad(squad)
                .user(user)
                .attendanceStatus(squad.getSquadAccessType() == SquadAccessType.OPEN
                        ? AttendanceStatus.APPROVED
                        : AttendanceStatus.PENDING)
                .build();
    }

    public void rejected() {
        setAttendanceStatus(AttendanceStatus.REJECTED);
    }
}
