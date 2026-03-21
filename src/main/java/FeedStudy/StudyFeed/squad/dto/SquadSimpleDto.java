package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.squad.entity.Squad;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor
public class SquadSimpleDto {

    private Long squadId;

    private Topic category;
    private String categoryLabel;

    private String name;
    private String regionLabel;
    private String genderLabel;

    private LocalDate date;
    private LocalTime time;
    private boolean timeSpecified;

    private LocalDateTime createdDate;

    private boolean closed;
    private int currentCount;
    private int maxParticipants;

    private int minAge;
    private int maxAge;

    // 화면 표시용
    private String ddayLabel;
    private String statusLabel;
    private String createdAgoLabel;
    private String peopleLabel;

    private String membership;   // OWNER / JOINED / null 가능
    private Long pendingCount;   // 승인형일 때만 값 존재

    /** Squad → DTO 변환 */
    public static SquadSimpleDto toDto(
            Squad squad,
            String membership,
            Long pendingCount
    ) {

        String regionLabel = squad.getRegionMain()
                             + ((squad.getRegionSub() != null && !"전체".equals(squad.getRegionSub()))
                ? " " + squad.getRegionSub()
                : "");

        String genderLabel = squad.getGenderRequirement().getDescription();

        String dday = calcDDayLabel(squad.getDate());
        String status = calcStatusLabel(squad);
        String createdAgo = calcAgoLabel(squad.getCreatedAt());
        String people = squad.getCurrentCount() + "/" + squad.getMaxParticipants();

        return new SquadSimpleDto(
                squad.getId(),
                squad.getCategory(),
                squad.getCategory().getDescription(),
                squad.getTitle(),
                regionLabel,
                genderLabel,
                squad.getDate(),
                squad.getTime(),
                squad.isTimeSpecified(),
                squad.getCreatedAt(),
                squad.isClosed(),
                squad.getCurrentCount(),
                squad.getMaxParticipants(),
                squad.getMinAge(),
                squad.getMaxAge(),
                dday,
                status,
                createdAgo,
                people,
                membership,
                pendingCount
        );
    }

    /** D-day 계산 */
    private static String calcDDayLabel(LocalDate date) {
        long diff = ChronoUnit.DAYS.between(LocalDate.now(), date);

        if (diff == 0) return "D-DAY";
        if (diff > 0) return "D-" + diff;

        return "D+" + Math.abs(diff);
    }

    /** 상태 계산 */
    private static String calcStatusLabel(Squad squad) {

        LocalDate today = LocalDate.now();

        boolean ended =
                squad.getDate().isBefore(today)
                || (
                        squad.getDate().isEqual(today)
                        && squad.isTimeSpecified()
                        && squad.getTime() != null
                        && LocalTime.now().isAfter(squad.getTime())
                );

        if (ended) return "종료";

        if (squad.isClosed() || squad.getCurrentCount() >= squad.getMaxParticipants()) {
            return "정원마감";
        }

        return "모집중";
    }

    /** 생성 시점 기준 경과 시간 */
    private static String calcAgoLabel(LocalDateTime created) {

        LocalDateTime now = LocalDateTime.now();

        long minutes = Duration.between(created, now).toMinutes();

        if (minutes < 1) return "방금";
        if (minutes < 60) return minutes + "분 전";

        long hours = minutes / 60;

        if (hours < 24) return hours + "시간 전";

        long days = hours / 24;

        return days + "일 전";
    }
}