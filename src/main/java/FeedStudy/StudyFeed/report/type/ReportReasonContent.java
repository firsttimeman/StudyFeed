package FeedStudy.StudyFeed.report.type;

public enum ReportReasonContent {
    VIOLATION_OF_SERVICE_POLICY("서비스 목적에 위반"),       //
    ADVERTISING_OR_PROMOTION("광고/홍보/로고"),          //
    FRAUD_OR_CRIME_RELATED("사기/범죄 등"),            //
    MONEY_TRANSACTION_GROUP("금전 거래"),           // 금전 거래
    OTHER("기타");                              // 기타

    private final String description;

    ReportReasonContent(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
