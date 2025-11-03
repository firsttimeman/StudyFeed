package FeedStudy.StudyFeed.report.entity;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.ReportStatus;
import FeedStudy.StudyFeed.report.type.ReportReasonContent;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "report_content",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"reporter_id", "feed_id"}),
                @UniqueConstraint(columnNames = {"reporter_id", "squad_id"})
        }
)
public class ReportContent extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed; // nullable: feed or squad 둘 중 하나만

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    @Enumerated(EnumType.STRING)
    private ReportReasonContent reason;

    private String content;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    public static ReportContent ofFeed(User reporter, Feed feed, ReportReasonContent reason, String content) {
        return new ReportContent(reporter, feed, null, reason, content, ReportStatus.PENDING);
    }

    public static ReportContent ofSquad(User reporter, Squad squad, ReportReasonContent reason, String content) {
        return new ReportContent(reporter, null, squad, reason, content, ReportStatus.PENDING);
    }


}
