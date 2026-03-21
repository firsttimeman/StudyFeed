package FeedStudy.StudyFeed.feed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "피드 ID는 필수입니다.")
    private Long feedId;

    @Schema(description = "댓글 내용", required = true)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 1000자 이하로 입력해주세요.")
    private String content;


}
