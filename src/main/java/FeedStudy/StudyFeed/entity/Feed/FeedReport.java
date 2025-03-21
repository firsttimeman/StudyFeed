package FeedStudy.StudyFeed.entity.Feed;

import FeedStudy.StudyFeed.entity.BaseEntity;
import FeedStudy.StudyFeed.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "feed_id"}
))
public class FeedReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Column(nullable = false, length = 25)
    private String reason;

    public FeedReport(User user, Feed feed, String reason) {
        this.user = user;
        this.feed = feed;
        this.reason = reason;
    }
}
