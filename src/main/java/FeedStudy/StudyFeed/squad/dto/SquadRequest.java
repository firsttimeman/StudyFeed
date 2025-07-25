package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "스쿼드 제목", example = "풋살할 사람 구해요!", minLength = 1, maxLength = 50)
    @Size(min = 1, max = 50)
    private String title;

    @Schema(description = "카테고리", example = "풋살", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String category;


    @Schema(description = "지역(대분류)", example = "서울", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String regionMain;

    @Schema(description = "지역(소분류)", example = "강남구", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String regionSub;

    @Schema(description = "설명", example = "20대 풋살팀입니다. 가볍게 운동해요!", maxLength = 1000)
    @Size(max = 1000)
    private String description;

    @Schema(description = "최소 연령")
    @Min(50)
    private int minAge;

    @Schema(description = "최대 연령")
    private int maxAge;


    @Schema(description = "모임 날짜", example = "2025-08-01")
    private LocalDate date;

    @Schema(description = "모임 시간", example = "19:30")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalTime time;

    @Schema(description = "시간 지정 여부", example = "true")
    private Boolean timeSpecified;

    @Schema(description = "성별 제한 (ALL, MALE, FEMALE)", example = "ALL")
    private Gender genderRequirement;

    @Schema(description = "참여 승인 방식 (AUTO, MANUAL)", example = "MANUAL")
    private JoinType joinType;


    @Schema(description = "최대 인원 수", example = "10")
    private int maxParticipants;


}
