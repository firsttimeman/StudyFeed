package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.FeedImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.parameters.P;

@AllArgsConstructor
@Data
public class ImageResponseDto<I> {
    private String url;
    private Long id;

    public ImageResponseDto(I image) {
        if (image instanceof FeedImage) {
            FeedImage feedImage = (FeedImage) image;
            url = feedImage.getImageUrl();
            id = feedImage.getId();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
