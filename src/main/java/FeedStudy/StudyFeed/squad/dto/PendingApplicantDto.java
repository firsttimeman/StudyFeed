package FeedStudy.StudyFeed.squad.dto;


import FeedStudy.StudyFeed.global.type.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PendingApplicantDto {

    private String nickname;
    private String profileUrl;
    private Gender gender;
    private LocalDate birth;
    private String email;
}