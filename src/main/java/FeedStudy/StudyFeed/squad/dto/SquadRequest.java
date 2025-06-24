package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.dto.RegionRequest;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
//implements RegionRequest
public class SquadRequest  {


    private String title, category, regionMain, regionSub, description;

    private int minAge;

    private int maxAge;

    private LocalDate date;

    private LocalTime time;

    private Boolean timeSpecified;

    private Gender genderRequirement;

    private JoinType joinType;

    private int maxParticipants;



}
