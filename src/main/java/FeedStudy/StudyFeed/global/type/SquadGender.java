package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SquadGender {

    ANY("누구나 참여 가능"),
    MALE_ONLY("남자만 가능"),
    FEMALE_ONLY("여자만 가능");

    private final String description;

    public boolean matches(Gender gender) {
        return this == ANY ||
                this == MALE_ONLY && gender == Gender.MALE ||
                this == FEMALE_ONLY && gender == Gender.FEMALE;
    }
}
