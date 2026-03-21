package FeedStudy.StudyFeed.feed.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@NoArgsConstructor
@ToString
@Table(name = "feed_image")
public class FeedImage extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    private String uniqueName;

    private String imageUrl;

    public static FeedImage fromUrl(String url) {
        FeedImage feedImage = new FeedImage();
        feedImage.imageUrl = url;
        return feedImage;
    }



    public void initFeed(Feed feed) {
        if(this.feed == null) {
            this.feed = feed;
        }
    }


}