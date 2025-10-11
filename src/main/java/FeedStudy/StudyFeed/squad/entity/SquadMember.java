package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.JoinType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "squad_member")
public class SquadMember extends BaseEntity {



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id", nullable = false)
    private Squad squad;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus attendanceStatus;

    @Column(nullable = false)
    private boolean isOwner;

    private SquadMember(User user, Squad squad) {
        this.user = user;
        this.squad = squad;
        this.isOwner = squad.getUser().getId().equals(user.getId());
        this.attendanceStatus = squad.getUser().getId() == user.getId() ||
                squad.getJoinType().equals(JoinType.DIRECT) ? AttendanceStatus.JOINED : AttendanceStatus.PENDING;
    }

    public static SquadMember create(User user, Squad squad) {
        return new SquadMember(user, squad);
    }



}
