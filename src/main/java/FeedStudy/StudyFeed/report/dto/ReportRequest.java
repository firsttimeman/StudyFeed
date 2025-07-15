package FeedStudy.StudyFeed.report.dto;

import FeedStudy.StudyFeed.report.type.ReportReasonContent;
import FeedStudy.StudyFeed.report.type.ReportReasonUser;
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

}
