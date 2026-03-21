package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
public class UpdateSquadRequest {

    @Schema(description = "스쿼드 제목", example = "제목 수정")
    @Size(min = 1, max = 50)
    private String title;

    @Schema(description = "카테고리", example = "HEALTH_FITNESS")
    private Topic category;

    @Schema(description = "지역(대분류)", example = "서울")
    private String regionMain;

    @Schema(description = "지역(소분류)", example = "강남구")
    private String regionSub;

    @Schema(description = "설명", example = "설명 수정")
    @Size(max = 1000)
    private String description;

    @Schema(description = "최소 연령", example = "20")
    @Min(20)
    @Max(100)
    private Integer minAge;

    @Schema(description = "최대 연령", example = "40")
    @Min(20)
    @Max(100)
    private Integer maxAge;

    @Schema(description = "모임 날짜", example = "2025-12-25")
    private LocalDate date;

    @Schema(description = "모임 시간", example = "19:30")
    private LocalTime time;

    @Schema(description = "시간 지정 여부", example = "true")
    private Boolean timeSpecified;

    @Schema(description = "성별 제한 (ALL, MALE, FEMALE)", example = "ALL")
    private Gender genderRequirement;

    @Schema(description = "참여 방식 (DIRECT 즉시 참여 / APPROVAL 승인 필요)", example = "APPROVAL")
    private JoinType joinType;

    @Schema(description = "최대 인원 수", example = "10")
    @Min(1)
    @Max(20)
    private Integer maxParticipants;
}