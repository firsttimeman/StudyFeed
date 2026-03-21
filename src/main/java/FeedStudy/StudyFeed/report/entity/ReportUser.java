package FeedStudy.StudyFeed.report.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.ReportStatus;
import FeedStudy.StudyFeed.report.type.ReportReasonUser;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "report_user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reporter_id", "reported_id"})
)
public class ReportUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReasonUser reportReasonUser;

    @Column(length = 300)
    private String content;

    public ReportUser(User reporter, User reported, ReportReasonUser reason, String content) {
        this.reporter = reporter;
        this.reported = reported;
        this.reportReasonUser = reason;
        this.content = content;
        this.status = ReportStatus.PENDING;
    }

    public void changeStatus(ReportStatus status) {
        this.status = status;
    }
}