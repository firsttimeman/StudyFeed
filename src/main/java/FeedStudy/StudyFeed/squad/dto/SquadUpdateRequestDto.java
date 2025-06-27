package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.dto.RegionRequest;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.Topic;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SquadUpdateRequestDto {


    private String squadName;

    private Topic topic;

    @NotNull
    private String regionMain;

    private String regionSub;

    private LocalDate meetDate;
    private LocalTime meetTime;

    private Gender gender;

//    private Age age;
    private Integer minAge;
    private Integer maxAge;

    private String description;

    private JoinType joinType;

    private Integer PeopleNum;


}
