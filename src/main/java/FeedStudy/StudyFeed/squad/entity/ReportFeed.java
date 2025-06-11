package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.ReportStatus;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ReportFeed extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private User reportedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feedId;

    private String category;

    private String content;

    private ReportStatus status;


    public ReportFeed(User reportedId, Feed feedId, String category, String content) {
        this.reportedId = reportedId;
        this.feedId = feedId;
        this.category = category;
        this.content = content;
    }

}
