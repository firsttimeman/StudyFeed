package FeedStudy.StudyFeed.user.dto;

import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class UserSimpleDto {

    private String nickname;
    private String profileUrl;
    private String gender;
    private LocalDate birth;

    public static UserSimpleDto toDto(User user) {

        return UserSimpleDto.builder()
                .nickname(user.getNickName())
                .profileUrl(user.getImageUrl())
                .gender(user.getGender())
                .birth(user.getBirthDate())
                .build();
    }
}
