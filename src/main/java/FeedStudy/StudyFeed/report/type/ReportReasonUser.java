package FeedStudy.StudyFeed.report.type;

public enum ReportReasonUser {
    MEET_UP_REQUEST("1:1 만남 요구"),
    PROMOTION("다른 외부 채팅방 또는 서비스로 유도"),
    INAPPROPRIATE_PROFILE("비하, 비방적 닉네임/태도"),
    SEXUAL("성적 수치심 유발 발언, 행동"),
    DISRUPTIVE_BEHAVIOR("욕설, 시비, 분란 등"),
    EXTREME_OPINION("특정 종교, 정치, 혐오 활동"),
    SPAM("무단 광고"),
    ETC("기타");

    private final String description;

    ReportReasonUser(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
