package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {

    ALL("누구나"),
    MALE_ONLY("남성만"),
    FEMALE_ONLY("여성만");

    private String gender;

    private Gender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return gender;
    }

}
