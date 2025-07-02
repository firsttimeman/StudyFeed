package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
//implements RegionRequest
public class SquadRequest {

    @Size(min = 1, max = 50)
    private String title;

    @NotNull
    private String category;
    @NotNull
    private String regionMain;
    @NotNull
    private String regionSub;

    @Size(max = 1000)
    private String description;

    @Min(50)
    private int minAge;

    private int maxAge;

    private LocalDate date;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalTime time;

    private Boolean timeSpecified;

    private Gender genderRequirement;

    private JoinType joinType;

    private int maxParticipants;


}
