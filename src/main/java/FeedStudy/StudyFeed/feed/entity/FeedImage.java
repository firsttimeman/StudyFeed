package FeedStudy.StudyFeed.feed.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString
@Table(name = "feed_image")
public class FeedImage extends BaseEntity {

    private final static String supportedExtensions[] = {"jpg", "jpeg", "png", "gif"};

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    private String uniqueName;
    private String imageUrl;

    // ✅ 정적 팩토리 추가: 서비스 코드에서 호출하는 메서드
    public static FeedImage ofOriginalName(String originalName) {
        return new FeedImage(originalName);
    }

    public FeedImage(String imageUrl , String originalName) {
        this.imageUrl = imageUrl;
        this.uniqueName = generateUniqueName(extractExtension(originalName));
    }
    public FeedImage(String originalName) {
        this.uniqueName = generateUniqueName(extractExtension(originalName));
    }

    public void initFeed(Feed feed) {
        if(this.feed == null) {
            this.feed = feed;
        }
    }

    private String generateUniqueName(String ext) {
        return UUID.randomUUID().toString().replace("-", "") + "." + ext;
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
        throw new FeedException(ErrorCode.IMAGE_EXT_NOT_SUPPORTED);
    }

    private boolean supportedExtensions(String ext) {
        return Arrays.stream(supportedExtensions).anyMatch(x -> x.equalsIgnoreCase(ext));
    }
}