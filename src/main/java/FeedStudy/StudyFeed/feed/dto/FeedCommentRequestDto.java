package FeedStudy.StudyFeed.feed.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedCommentRequestDto {

    @NotNull
    private Long commentId;

    @NotNull
    private String comment;

    @NotNull
    private Long feedId;
}
