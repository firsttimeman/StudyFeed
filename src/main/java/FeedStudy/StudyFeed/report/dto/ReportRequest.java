package FeedStudy.StudyFeed.report.dto;

import FeedStudy.StudyFeed.report.type.ReportReasonContent;
import FeedStudy.StudyFeed.report.type.ReportReasonUser;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {

    @Size(max = 300, message = "신고 내용은 300자 이하여야 합니다.")
    private String content;

    private ReportReasonUser reportReasonUser;
    private ReportReasonContent reportReasonContent;

    @AssertTrue(message = "사용자 신고 사유와 컨텐츠 신고 사유 중 하나만 선택해야 합니다.")
    public boolean isValidReportType() {
        return (reportReasonUser != null) ^ (reportReasonContent != null);
    }
}