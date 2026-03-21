package FeedStudy.StudyFeed.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedLikeDto {
    private boolean like;

    public static FeedLikeDto toDto(boolean isLike) {
        return new FeedLikeDto(isLike);
    }
}
