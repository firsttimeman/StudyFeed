package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MembershipStatus {
    JOINED,     // 최종 멤버 (활동 가능)
    PENDING,    // 승인 대기
    REJECTED,   // 신청 거절
    KICKED_OUT  // 강퇴
}
