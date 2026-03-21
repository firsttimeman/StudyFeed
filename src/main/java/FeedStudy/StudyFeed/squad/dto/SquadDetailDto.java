package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class SquadDetailDto {

    private String status;                 // "모집 중" / "모집 마감" / "종료"
    private Topic category;
    private String categoryLabel;
    private String title;
    private String description;
    private String datetime;
    private String region;
    private String genderRequirement;
    private String ageRequirement;

    private int maxParticipantCount;
    private int currentParticipantCount;
    private long ownerId;

    // 프론트 분기용
    private boolean isOwner;
    private boolean hasPendingUsers;
    private boolean isClosed;
    private boolean isExpired;
    private String myMembership; // "OWNER","JOINED","PENDING","REJECTED","KICKED_OUT","LEFT","NONE"

    private List<UserSimpleDto> participants;

    public static SquadDetailDto toDto(
            User user,
            Squad squad,
            List<UserSimpleDto> participants,
            boolean hasPendingUsers,
            String myMembership
    ) {
        String status = calculateStatusText(squad);
        Topic category = squad.getCategory();
        String categoryLabel = squad.getCategory().getDescription();
        String title = squad.getTitle();
        String description = squad.getDescription();
        String datetime = formatDateTime(squad);
        String region = formatRegion(squad);
        String genderRequirement = squad.getGenderRequirement().getDescription();
        String ageRequirement = squad.getMinAge() + "세 ~ " + squad.getMaxAge() + "세";

        int maxParticipantCount = squad.getMaxParticipants();
        int currentParticipantCount = squad.getCurrentCount();
        long ownerId = squad.getUser().getId();

        boolean isOwner = Objects.equals(ownerId, user.getId());
        boolean isClosed = squad.isClosed();
        boolean isExpired = isExpired(squad);

        return new SquadDetailDto(
                status,
                category,
                categoryLabel,
                title,
                description,
                datetime,
                region,
                genderRequirement,
                ageRequirement,
                maxParticipantCount,
                currentParticipantCount,
                ownerId,
                isOwner,
                hasPendingUsers,
                isClosed,
                isExpired,
                myMembership,
                participants
        );
    }

    private static String formatRegion(Squad squad) {
        if ("전체".equals(squad.getRegionMain())) {
            return "전체";
        }

        if (squad.getRegionSub() == null || squad.getRegionSub().isBlank() || "전체".equals(squad.getRegionSub())) {
            return squad.getRegionMain();
        }

        return squad.getRegionMain() + " " + squad.getRegionSub();
    }

    private static boolean isExpired(Squad squad) {
        LocalDate today = LocalDate.now();

        if (squad.getDate().isBefore(today)) {
            return true;
        }

        if (squad.getDate().isEqual(today) && squad.isTimeSpecified() && squad.getTime() != null) {
            return squad.getTime().isBefore(LocalTime.now());
        }

        return false;
    }

    private static String calculateStatusText(Squad squad) {
        if (isExpired(squad)) {
            return "종료";
        }

        if (squad.isClosed() || squad.getCurrentCount() >= squad.getMaxParticipants()) {
            return "모집 마감";
        }

        return "모집 중";
    }

    private static String formatDateTime(Squad squad) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E", Locale.KOREAN);
        String dateText = squad.getDate().format(formatter);

        if (!squad.isTimeSpecified() || squad.getTime() == null) {
            return dateText;
        }

        int hour = squad.getTime().getHour();
        int minute = squad.getTime().getMinute();

        String period = hour < 12 ? "오전" : "오후";
        int displayHour = (hour % 12 == 0) ? 12 : (hour % 12);

        StringBuilder timeText = new StringBuilder();
        timeText.append(period).append(" ").append(displayHour).append("시");

        if (minute != 0) {
            timeText.append(" ").append(minute).append("분");
        }

        return dateText + " " + timeText;
    }
}