package FeedStudy.StudyFeed.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FeedResponseDto {
    private boolean hasNext;
    private List<FeedSimpleDto> list;
}
