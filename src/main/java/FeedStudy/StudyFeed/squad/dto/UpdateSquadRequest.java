package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private String title;

    @Schema(description = "카테고리", example = "HEALTH_FITNESS")
    private Topic category;

    @Schema(description = "지역(대분류)", example = "서울")
    private String regionMain;

    @Schema(description = "지역(소분류)", example = "강남구")
    private String regionSub;

    @Schema(description = "설명", example = "설명 수정")
    private String description;

    @Schema(description = "최소 연령", example = "50")
    private Integer minAge;          // ✅ 래퍼 타입

    @Schema(description = "최대 연령", example = "80")
    private Integer maxAge;          // ✅ 래퍼 타입

    @Schema(description = "모임 날짜", example = "2025-12-25")
    private LocalDate date;

    @Schema(description = "모임 시간", example = "19:30")
    private LocalTime time;

    @Schema(description = "시간 지정 여부", example = "true")
    private Boolean timeSpecified;   // ✅ null이면 유지

    @Schema(description = "성별 제한 (ALL, MALE, FEMALE)", example = "ALL")
    private Gender genderRequirement;

    @Schema(description = "참여 승인 방식 (AUTO, APPROVAL)", example = "APPROVAL")
    private JoinType joinType;

    @Schema(description = "최대 인원 수", example = "10")
    private Integer maxParticipants; // ✅ 래퍼 타입

}
