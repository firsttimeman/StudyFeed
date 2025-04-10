package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_name", nullable = false)
    private Squad squad;

    @Column(nullable = false, length = 500)
    private String reason;


    public static SquadReport createSquadReport(User reporter, User reportedUser, Squad squad, String reason) {

        return SquadReport.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .squad(squad)
                .reason(reason)
                .build();

    }


}
