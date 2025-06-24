package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {

    ALL("누구나"),
    MALE("남성만"),
    FEMALE("여성만");

    private String gender;

    private Gender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return gender;
    }

}
