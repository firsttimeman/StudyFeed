package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.type.JoinType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "squad_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"squad_id", "user_id"})
)
public class SquadMember extends BaseEntity {



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id", nullable = false)
    private Squad squad;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus membershipStatus;

    @Column(nullable = false)
    private boolean owner;

    private SquadMember(User user, Squad squad, boolean owner, MembershipStatus status) {
        this.user = user;
        this.squad = squad;
        this.owner = owner;
        this.membershipStatus = status;
    }

    /** 스쿼드 리더용: 무조건 JOINED + owner=true */
    public static SquadMember createOwner(User user, Squad squad) {
        return new SquadMember(user, squad, true, MembershipStatus.JOINED);
    }

    /** 즉시 참여(DIRECT)용: JOINED + owner=false */
    public static SquadMember createJoined(User user, Squad squad) {
        return new SquadMember(user, squad, false, MembershipStatus.JOINED);
    }

    /** 승인형(APPROVAL) 신청: PENDING + owner=false */
    public static SquadMember createPending(User user, Squad squad) {
        return new SquadMember(user, squad, false, MembershipStatus.PENDING);
    }


    public void approve()   { this.membershipStatus = MembershipStatus.JOINED; }
    public void reject()    { this.membershipStatus = MembershipStatus.REJECTED; }
    public void kickOut()   { this.membershipStatus = MembershipStatus.KICKED_OUT; }

    public boolean isOwner() { return owner; }


}
