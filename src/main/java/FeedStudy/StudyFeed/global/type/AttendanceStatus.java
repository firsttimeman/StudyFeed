package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AttendanceStatus {
    JOINED,
    PENDING,
    REJECTED,
    KICKED_OUT

}

