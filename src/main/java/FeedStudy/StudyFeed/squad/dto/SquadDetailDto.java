package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class SquadDetailDto {

    private String status;                 // "모집 중" / "모집 마감"
    private Topic category;
    private String title;
    private String description;
    private String datetime;
    private String region;
    private String genderRequirement;
    private String ageRequirement;

    // 숫자/상태
    private int     maxParticipantCount;
    private long    ownerId;

    // 행동 판단용 상태 신호 (프론트에서 버튼/문구 결정)
    private boolean isOwner;
    private boolean hasPendingUsers;
    private boolean isClosed;
    private boolean isExpired;
    private String  myMembership;          // "OWNER","JOINED","PENDING","REJECTED","KICKED","NONE"

    // 참가자(조인 필요: fetch join으로 N+1 방지)
    private List<UserSimpleDto> participants;

    public static SquadDetailDto toDto(User user, Squad squad,
                                       List<UserSimpleDto> participants,
                                       boolean hasPendingUsers,
                                       String myMembership) {
        // 기존 계산 재사용
        String status   = calculateStatusText(squad);
        Topic category  = squad.getCategory();
        String title    = squad.getTitle();
        String desc     = squad.getDescription();
        String datetime = formatDateTime(squad);
        String region   = "전체".equals(squad.getRegionMain())
                ? "전체" : squad.getRegionMain() + " " + squad.getRegionSub();
        String gender   = squad.getGenderRequirement().getName();
        String ageReq   = squad.getMinAge() + "세 ~ " + squad.getMaxAge() + "세";
        int maxPart     = squad.getMaxParticipants();
        long ownerId    = squad.getUser().getId();

        boolean isOwner = Objects.equals(ownerId, user.getId());
        boolean isClosed = squad.isClosed();
        boolean isExpired = isExpired(squad);

        return new SquadDetailDto(
                status, category, title, desc, datetime, region,
                gender, ageReq,
                maxPart, ownerId,
                isOwner, hasPendingUsers, isClosed, isExpired, myMembership,
                participants
        );
    }

    private static boolean isExpired(Squad squad) {
        LocalDate today = LocalDate.now();
        if (squad.getDate().isBefore(today)) return true;
        if (squad.getDate().isEqual(today) && squad.getTime() != null) {
            return squad.getTime().isBefore(LocalTime.now());
        }
        return false;
    }


    private static String calculateStatusText(Squad squad) {
        return (squad.isClosed() || isExpired(squad)) ? "모집 마감" : "모집 중";
    }

    private static String formatDateTime(Squad squad) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E", Locale.KOREAN);
        String dateStr = squad.getDate().format(fmt);
        if (squad.getTime() == null) return dateStr;

        int hour = squad.getTime().getHour();
        int minute = squad.getTime().getMinute();

        String period = hour < 12 ? "오전" : "오후";
        int displayHour = (hour % 12 == 0) ? 12 : (hour % 12);

        StringBuilder timeStr = new StringBuilder(period + " " + displayHour + "시");
        if (minute != 0) timeStr.append(" ").append(minute).append("분");

        return dateStr + " " + timeStr;
    }
}