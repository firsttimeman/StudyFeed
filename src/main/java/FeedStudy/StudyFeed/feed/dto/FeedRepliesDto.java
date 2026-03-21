package FeedStudy.StudyFeed.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedRepliesDto {

    private boolean hasNext;
    private List<FeedCommentDto> list;
}
