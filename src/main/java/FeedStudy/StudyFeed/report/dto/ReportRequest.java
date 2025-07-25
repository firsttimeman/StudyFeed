package FeedStudy.StudyFeed.report.dto;

import FeedStudy.StudyFeed.report.type.ReportReasonContent;
import FeedStudy.StudyFeed.report.type.ReportReasonUser;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {

    private String content;

    private ReportReasonUser reportReasonUser;
    private ReportReasonContent reportReasonContent;

    @AssertTrue(message = "사용자 신고 또는 컨텐츠 신고 사유 중 하나는 반드시 선택해야 합니다.")
    public boolean isValidReportType() {
        return reportReasonUser != null || reportReasonContent != null;
    }

}
