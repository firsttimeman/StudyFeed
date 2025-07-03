package FeedStudy.StudyFeed.feed.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false)
    private String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FeedComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedComment> childComments = new ArrayList<>();

    private boolean deleted = false;

    public FeedComment(User user, Feed feed, String content, FeedComment parentComment) {
        this.user = user;
        this.feed = feed;
        this.content = content;
        this.parentComment = parentComment;
    }

    public void markAsDeleted() {
        this.deleted = true;
        this.content = "작성자에 의해 삭제된 댓글 입니다.";
    }


}
