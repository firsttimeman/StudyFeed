package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class SquadDetailDto {

    private String status;
    private String category;
    private String title;
    private String description;
    private String datetime;
    private String region;
    private String genderRequirement;
    private String ageRequirement;
    private int maxParticipantCount;
    private boolean isOwner;
    private boolean hasPendingUsers;
    private List<UserSimpleDto> participants;
    private long ownerId;
    private String btnMsg;
    private boolean btnEnabled;


    public static SquadDetailDto toDto(User user, Squad squad) {
        String status = calculateStatus(squad);
        String category = squad.getCategory();
        String title = squad.getTitle();
        String description = squad.getDescription();
        String datetime = formatDateTime(squad);
        String region;

        if(squad.getRegionMain().equals("전체")) {
            region = "전체";
        } else {
            region = squad.getRegionMain() + " " + squad.getRegionSub();
        }

        String genderRequirement = squad.getGenderRequirement().getName();
        String ageRequirement = new StringBuilder().append(squad.getMinAge()).append("세 ~ ").append(squad.getMaxAge())
                .append("세").toString();
        int maxParticipants = squad.getMaxParticipants();
        boolean isOwner = squad.getUser().getId() == user.getId();
        boolean hasPendingUsers = squad.getMembers().stream()
                .anyMatch(member -> member.getAttendanceStatus() == AttendanceStatus.PENDING);
        List<UserSimpleDto> participants = squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED)
                .map(m -> UserSimpleDto.toDto(m.getUser())).toList();
        Long ownerId = squad.getUser().getId();
        String btnMsg;
        boolean btnEnabled = false;

        if(user.equals(squad.getUser())) {
            btnMsg = "대화방 가기";
            btnEnabled = true;
        } else {
            Optional<SquadMember> squadMember = squad.getMembers().stream()
                    .filter(member -> member.getUser().equals(user))
                    .findAny();

            if(squadMember.isPresent()) {
                SquadMember member = squadMember.get();

                switch(member.getAttendanceStatus()) {
                    case JOINED -> {
                        btnMsg = "대화방 가기";
                        btnEnabled = true;
                        break;
                    }
                    case PENDING -> {
                        btnMsg = "참여 승인을 기다리는중...";
                        break;
                    }
                    case REJECTED ->  {
                        btnMsg = "참여가 거절된 모임";
                        break;
                    }
                    case KICKED_OUT ->  {
                        btnMsg = "모임장에 의해 내보내진 모임";
                        break;
                    }
                    default ->  {
                        btnMsg = "대화방 가기";
                        btnEnabled = true;
                    }

                }
            } else if(squad.isClosed()) {
                btnMsg = "마감된 모임";
            } else {
                LocalDateTime squadDateTime = LocalDateTime.of(
                        squad.getDate(),
                        squad.getTime() != null ? squad.getTime() : LocalTime.of(23, 59, 59));
                if(squadDateTime.isBefore(LocalDateTime.now())) {
                    btnMsg = "종료된 모임";
                } else {
                    btnMsg = "참여하기";
                    btnEnabled = true;
                }
            }
        }
        return new SquadDetailDto(status, category, title, description, datetime, region,
                genderRequirement, ageRequirement, maxParticipants, isOwner,
                hasPendingUsers, participants, ownerId, btnMsg, btnEnabled);
    }

    private static String formatDateTime(Squad squad) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E", Locale.KOREAN);
        String dateStr = squad.getDate().format(dateTimeFormatter);

        if(squad.getTime() == null) {
            return dateStr;
        }

        int hour = squad.getTime().getHour();
        int minute = squad.getTime().getMinute();

        String period = hour < 12 ? "오전" : "오후";
        int displayHour = hour % 12 == 0 ? 12 : hour % 12;

        StringBuilder timeStr = new StringBuilder(period + " " + displayHour + "시");
        if (minute != 0) {
            timeStr.append(" ").append(minute).append("분");
        }

        return dateStr + " " + timeStr.toString();
    }

    private static String calculateStatus(Squad squad) {
        if(squad.isClosed() || squad.getDate().isBefore(LocalDate.now()) ||
           squad.getDate().isEqual(LocalDate.now()) && squad.getTime() != null
           && squad.getTime().isBefore(LocalTime.now())) {
            return "모집 마감";
        }
        return "모집 중";
    }

}
