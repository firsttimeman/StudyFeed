package FeedStudy.StudyFeed.report.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.ReportStatus;
import FeedStudy.StudyFeed.report.type.ReportReasonUser;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "report_user")
public class ReportUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reportedId;

    private String category;

    private String content;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private ReportReasonUser reportReasonUser;

    public ReportUser(User reporterId, User reportedId, ReportReasonUser reportReasonUser, String content) {
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.reportReasonUser = reportReasonUser;
        this.content = content;
    }

}
