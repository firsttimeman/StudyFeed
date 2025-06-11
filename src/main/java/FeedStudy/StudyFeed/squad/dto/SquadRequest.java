package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.dto.RegionRequest;
import FeedStudy.StudyFeed.global.type.SquadAccessType;
import FeedStudy.StudyFeed.global.type.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SquadRequest implements RegionRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String squadName;

    private String title, category, regionMain, regionSub, description;

    private int minAge;

    private int maxAge;

    private LocalDate meetDate;

    private LocalTime meetTime;

    private Boolean timeSpecified;

    private Gender genderRequirement;

    private SquadAccessType  squadAccessType;

    private int maxParticipants;



}
