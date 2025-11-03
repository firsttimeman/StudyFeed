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
    @Schema(description = "대댓글 작성 시 부모 댓글 ID (루트 댓글일 경우 null)")
    private Long parentCommentId;

    @Schema(description = "작성할 피드 ID", required = true)
    @NotNull
    private Long feedId;

    @Schema(description = "댓글 내용", required = true)
    @NotNull
    private String content;


}
