package FeedStudy.StudyFeed.global.type;

import lombok.Getter;

@Getter
public enum Gender {

    ALL("전체"),
    MALE("남성"),
    FEMALE("여성");

    private final String description;

    Gender(String description) {
        this.description = description;
    }
}
