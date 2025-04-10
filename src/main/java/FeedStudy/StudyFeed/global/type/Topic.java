package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Topic {

    HEALTH_FITNESS("건강/운동"),
    FOOD_CAFE("맛집/카페"),
    HOBBY("취미"),
    TRAVEL("나들이/여행"),
    FINANCE("재테크"),
    DAILY_LIFE("일상"),
    OTHER("기타");

    private final String description;

}
