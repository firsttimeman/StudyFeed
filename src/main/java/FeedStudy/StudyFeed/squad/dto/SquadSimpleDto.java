package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.squad.entity.Squad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@AllArgsConstructor
public class SquadSimpleDto {

    private Long pid;
    private Topic category;         // ✅ String → Topic
    private String categoryLabel;   // ✅ 화면 표시용 (예: “건강/운동”)
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
    private String ddayLabel;        // D-3 / D-DAY / D+1
    private String statusLabel;      // "모집중" / "정원마감" / "종료"
    private String createdAgoLabel;  // "방금" / "1일 전" / "1시간 전" 등
    private String peopleLabel;      // "3/5"
    private String membership;       // "OWNER" / "JOINED"
    private Long pendingCount;       // (옵션) 오너+승인형일 때만

    /** ✅ Squad → DTO 변환 */
    public static SquadSimpleDto toDto(
            Squad s,
            String membership,      // "OWNER"/"JOINED"
            Long pendingCountOpt    // null 허용
    ) {
        String regionLabel = s.getRegionMain()
                             + ((s.getRegionSub() != null && !"전체".equals(s.getRegionSub()))
                ? " " + s.getRegionSub() : "");

        String genderLabel = s.getGenderRequirement().getGender();

        String dday = calcDDayLabel(s.getDate());
        String status = calcStatusLabel(s);
        String createdAgo = calcAgoLabel(s.getCreatedAt());
        String people = s.getCurrentCount() + "/" + s.getMaxParticipants();

        return new SquadSimpleDto(
                s.getId(),
                s.getCategory(),                        // ✅ Topic enum 그대로
                s.getCategory().getDescription(),        // ✅ 화면용 한글 라벨
                s.getTitle(),
                regionLabel,
                genderLabel,
                s.getDate(),
                s.getTime(),
                s.isTimeSpecified(),
                s.getCreatedAt(),
                s.isClosed(),
                s.getCurrentCount(),
                s.getMaxParticipants(),
                s.getMinAge(),
                s.getMaxAge(),
                dday,
                status,
                createdAgo,
                people,
                membership == null || membership.isBlank() ? "JOINED" : membership,
                pendingCountOpt
        );
    }

    /** D-day 계산 */
    private static String calcDDayLabel(LocalDate date) {
        long diff = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (diff == 0) return "D-DAY";
        return diff > 0 ? "D-" + diff : "D+" + Math.abs(diff);
    }

    /** 상태 계산 */
    private static String calcStatusLabel(Squad s) {
        LocalDate today = LocalDate.now();
        boolean ended =
                s.getDate().isBefore(today)
                || (s.getDate().isEqual(today)
                    && s.isTimeSpecified()
                    && s.getTime() != null
                    && LocalTime.now().isAfter(s.getTime()));
        if (ended) return "종료";
        if (s.isClosed() || s.getCurrentCount() >= s.getMaxParticipants()) return "정원마감";
        return "모집중";
    }

    /** 생성 시점으로부터 경과 시간 계산 */
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