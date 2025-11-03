package FeedStudy.StudyFeed.feed.entity;

import FeedStudy.StudyFeed.feed.dto.FeedRequest;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "feed")
public class Feed extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Topic category;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int reportCount = 0;

    @Column(nullable = false)
    private int viewCount = 0;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedComment> comments = new ArrayList<>();

    /** 정적 팩토리: 생성 책임을 명확하게 */
    public static Feed create(User user, String content, Topic category) {
        Feed f = new Feed();
        f.user = user;
        f.content = content;
        f.category = category;
        return f;
    }

    public void update(String content, Topic category) {
        this.content = content;
        this.category = category;
    }

    /** 양방향 연관 편의 메서드 */
    public void addImages(List<FeedImage> images) {
        if (images == null || images.isEmpty()) return;
        for (FeedImage img : images) {
            this.images.add(img);
            img.initFeed(this);
        }
    }

    public void increaseLikeCount() {
        this.likeCount = this.likeCount + 1;
    }
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount = this.likeCount - 1;
        }
    }
}