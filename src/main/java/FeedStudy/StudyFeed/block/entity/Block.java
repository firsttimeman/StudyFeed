package FeedStudy.StudyFeed.block.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "user_block",
       uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"})
)
@NoArgsConstructor
public class Block extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    public Block(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
    }

}
