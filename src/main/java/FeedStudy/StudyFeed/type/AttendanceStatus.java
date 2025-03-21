package FeedStudy.StudyFeed.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AttendanceStatus {
    PENDING("대기중"),    // 대기 중
    APPROVED("승인됨"),   // 승인됨 (참가 확정)
    REJECTED("거절됨"),
    BANNED("강퇴"),
    WITHDRAW("퇴장");

    private final String description;
}
