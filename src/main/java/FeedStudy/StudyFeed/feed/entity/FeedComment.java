package FeedStudy.StudyFeed.feed.entity;


import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// FeedComment.java
@Entity
@Getter
@NoArgsConstructor
@Table(name = "feed_comment")
public class FeedComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // 계정 삭제 시 null 허용 정책이면 유지
    private User user;

    @Lob
    @Column(nullable = false)
    private String content;

    // 부모만 보유(양방향 child 컬렉션 제거)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FeedComment parentComment;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private int replyCount = 0; // 대댓글 수 캐시

    public FeedComment(User user, Feed feed, String content, FeedComment parent) {
        this.user = user;
        this.feed = feed;
        this.content = content;
        this.parentComment = parent;
    }

    /** 소프트 삭제 (대댓글이 있는 루트 댓글 등) */
    public void markAsDeleted() {
        this.deleted = true;
        this.content = "작성자에 의해 삭제된 댓글 입니다.";
    }
}