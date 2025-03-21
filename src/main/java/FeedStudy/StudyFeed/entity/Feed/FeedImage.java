package FeedStudy.StudyFeed.entity.Feed;

import FeedStudy.StudyFeed.entity.BaseEntity;
import FeedStudy.StudyFeed.exception.ErrorCode;
import FeedStudy.StudyFeed.exception.exceptiontype.FeedException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class FeedImage extends BaseEntity {

    private final static String supportedExtensions[] = {"jpg", "jpeg", "png", "gif"};

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Column(nullable = false)
    private String uniqueName;

    public FeedImage(String originalName) {
        this.uniqueName = generateUniqueName(extractExtension(originalName));
    }

    public void initFeed(Feed feed) {
        if(this.feed == null) {
            this.feed = feed;
        }
    }



    private String generateUniqueName(String ext) {
        return UUID.randomUUID().toString().replace("-", "") + ext;
    }

    private String extractExtension(String fileName) {
        try {
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            if(supportedExtensions(ext)) {
                return ext;
            }
        } catch (Exception e) {
            throw new FeedException(ErrorCode.IMAGE_EXT_NOT_SUPPORTED);
        }
        return null;
    }

    private boolean supportedExtensions(String ext) {
        return Arrays.stream(supportedExtensions).anyMatch(x -> x.equalsIgnoreCase(ext));
    }

}
