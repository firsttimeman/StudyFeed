package FeedStudy.StudyFeed.feed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FeedCommentRequestDto {

    @Schema(description = "작성할 피드 코멘트 id")
    private Long feedCommentPid;

    @Schema(description = "작성할 내용")
    @NotNull
    private String content;

    @Schema(description = "작성할 피드의 id")
    @NotNull
    private Long feedPid;


}
