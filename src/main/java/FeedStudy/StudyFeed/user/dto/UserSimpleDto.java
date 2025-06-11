package FeedStudy.StudyFeed.user.dto;

import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UserSimpleDto {

    private String nickname;
    private String profileUrl;
    private String gender;
    private LocalDate birth;

    public static UserSimpleDto toDto(User user) {

        String nickname = user.getNickName();
        String profileUrl = user.getImageUrl();
        String gender = user.getGender();
        LocalDate birth = user.getBirthDate();
        return new UserSimpleDto(nickname, profileUrl, gender, birth);
    }
}
