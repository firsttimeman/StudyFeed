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
@Table(
        name = "report_user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reporter_id", "reported_id"})
)
public class ReportUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;  // ✅ 변경

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;  // ✅ 변경

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private ReportReasonUser reportReasonUser;

    private String content;

    public ReportUser(User reporter, User reported, ReportReasonUser reason, String content) {
        this.reporter = reporter;
        this.reported = reported;
        this.reportReasonUser = reason;
        this.content = content;
    }
}