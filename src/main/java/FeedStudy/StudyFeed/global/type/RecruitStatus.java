package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RecruitStatus {
    OPEN("모집중"),
    CLOSED("모집 종료"),
    DISBANDED("모임 해체");

    private final String description;
}
