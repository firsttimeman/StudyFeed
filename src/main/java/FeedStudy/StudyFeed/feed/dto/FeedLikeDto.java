package FeedStudy.StudyFeed.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedLikeDto {
    private boolean like;

    private int count;

    public static FeedLikeDto toDto(boolean isLike, int count) {
        return new FeedLikeDto(isLike, count);
    }
}
