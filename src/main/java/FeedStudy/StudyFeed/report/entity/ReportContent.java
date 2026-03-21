package FeedStudy.StudyFeed.report.entity;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.ReportStatus;
import FeedStudy.StudyFeed.report.type.ReportReasonContent;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReasonContent reason;

    @Column(length = 300)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    private ReportContent(
            User reporter,
            Feed feed,
            Squad squad,
            ReportReasonContent reason,
            String content,
            ReportStatus status
    ) {
        this.reporter = reporter;
        this.feed = feed;
        this.squad = squad;
        this.reason = reason;
        this.content = content;
        this.status = status;
    }

    public static ReportContent ofFeed(User reporter, Feed feed, ReportReasonContent reason, String content) {
        return new ReportContent(reporter, feed, null, reason, content, ReportStatus.PENDING);
    }

    public static ReportContent ofSquad(User reporter, Squad squad, ReportReasonContent reason, String content) {
        return new ReportContent(reporter, null, squad, reason, content, ReportStatus.PENDING);
    }

    public void changeStatus(ReportStatus status) {
        this.status = status;
    }
}