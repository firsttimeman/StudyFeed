package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.squad.entity.Squad;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class SquadDto {

    private Long id;
    private String title;
    private Topic category;
    private String regionMain;
    private String regionSub;
    private String description;
    private int minAge;
    private int maxAge;
    private LocalDate date;
    private LocalTime time;
    private Boolean timeSpecified;
    private Gender genderRequirement;
    private JoinType joinType;
    private int maxParticipants;

    public static SquadDto from(Squad squad) {
        return SquadDto.builder()
                .id(squad.getId())
                .title(squad.getTitle())
                .category(squad.getCategory())
                .regionMain(squad.getRegionMain())
                .regionSub(squad.getRegionSub())
                .description(squad.getDescription())
                .minAge(squad.getMinAge())
                .maxAge(squad.getMaxAge())
                .date(squad.getDate())
                .time(squad.getTime())
                .timeSpecified(squad.isTimeSpecified())
                .genderRequirement(squad.getGenderRequirement())
                .joinType(squad.getJoinType())
                .maxParticipants(squad.getMaxParticipants())
                .build();
    }

}
