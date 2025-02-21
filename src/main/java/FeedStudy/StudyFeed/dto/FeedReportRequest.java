package FeedStudy.StudyFeed.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedReportRequest {

    @NotBlank(message = "신고 사유는 필수입니다.")
    private String reason;
}
