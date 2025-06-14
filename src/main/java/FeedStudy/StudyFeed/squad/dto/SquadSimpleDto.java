package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.squad.entity.Squad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class SquadSimpleDto {
    private Long pid;
    private String category;
    private String name;
    private String region;
    private String gender;
    private LocalDate date;
    private LocalTime time;
    private boolean timeSpecified;
    private LocalDateTime createdDate;
    private boolean closed;
    private int currentCount;
    private int maxParticipants;
    private int minAge;
    private int maxAge;

    public static SquadSimpleDto toDto(Squad squad) {
        StringBuilder sb = new StringBuilder(squad.getRegionMain());
        if(squad.getRegionSub().equals("전체")) {
            sb.append(" ").append(squad.getRegionSub());
        }

        return new SquadSimpleDto(
                squad.getId(),
                squad.getCategory(),
                squad.getTitle(),
                sb.toString(),
                squad.getGenderRequirement().getGender(),
                squad.getDate(),
                squad.getTime(),
                squad.isTimeSpecified(),
                squad.getCreatedAt(),
                squad.isClosed(),
                squad.getCurrentCount(),
                squad.getMaxParticipants(),
                squad.getMinAge(),
                squad.getMaxAge());

    }

}
