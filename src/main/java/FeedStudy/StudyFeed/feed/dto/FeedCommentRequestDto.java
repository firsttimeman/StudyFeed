package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.FeedComment;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedCommentRequestDto {

    private Long commentId;

    @NotNull
    private String comment;

    @NotNull
    private Long feedId;


}
