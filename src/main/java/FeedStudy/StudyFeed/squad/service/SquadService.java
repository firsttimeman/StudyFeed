package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.squad.dto.SquadDetailDto;
import FeedStudy.StudyFeed.squad.dto.SquadFilterRequest;
import FeedStudy.StudyFeed.squad.dto.SquadRequest;
import FeedStudy.StudyFeed.squad.dto.SquadSimpleDto;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.squad.util.ChatTokenProvider;
import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SquadService extends ASquadService {


    private final JwtUtil jwtUtil;
    private final ChatTokenProvider chatTokenProvider;

    public SquadService(SquadRepository squadRepository, UserRepository userRepository, SquadMemberRepository squadMemberRepository, FirebaseMessagingService firebaseMessagingService, BlockRepository blockRepository, JwtUtil jwtUtil, ChatTokenProvider chatTokenProvider) {
        super(squadRepository, userRepository, squadMemberRepository, firebaseMessagingService, blockRepository);
        this.jwtUtil = jwtUtil;
        this.chatTokenProvider = chatTokenProvider;
    }


    @Transactional
    public Squad createSquad(SquadRequest req, User user) {

        if(req.getMinAge() > req.getMaxAge()) {
            throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
        }



        Squad squad = Squad.create(user ,req);

        squad = squadRepository.save(squad);

        SquadMember squadMember = SquadMember.create(user, squad);
        squadMemberRepository.save(squadMember);
        return squad;
    }

    @Transactional
    public Squad updateSquad(Long squadId, User user, SquadRequest req) {
        Squad squad = findSquad(squadId);
        validateOwner(user, squad);

        validateAgeRange(squad, req);

        validateGender(squad, req);

        validateMemberCount(squad, req);

        squad.update(req);

        squadRepository.save(squad);
        return squad;
    }

    @Transactional
    public void deleteSquad(Long squadId, User user, boolean isForcedDelete) {

        Squad squad = findSquad(squadId);
        validateOwner(user, squad);
        validateDeleteMember(squad, isForcedDelete);
        List<SquadMember> members = squad.getMembers();
        squad.getMembers().clear();
        squadMemberRepository.deleteAll(members);
        squadRepository.delete(squad);
    }

    public DataResponse mySquad(User user, Pageable pageable) {
        Page<Squad> squads = squadRepository.findByUser(user, pageable);
        List<SquadSimpleDto> squadDtos = squads.getContent().stream().map(SquadSimpleDto::toDto).toList();
        return new DataResponse(squadDtos, squads.hasNext());

    }

    public Map<String, String> joinOrGetChatToken(User user, Long squadId) {

        Squad squad = squadRepository.findByIdWithParticipants(squadId)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));

        Optional<SquadMember> members = squadMemberRepository.findBySquadAndUser(squad, user);

        if(members.isPresent()) {
            SquadMember squadMember = members.get();

            return switch (squadMember.getAttendanceStatus()) {
                case JOINED -> Map.of("status", "joined", "chatToken", chatTokenProvider.createChatToken(user, squad));
                case PENDING -> Map.of("status", "pending");
                case REJECTED -> throw new SquadException(ErrorCode.SQUAD_REJECTED);
                case KICKED_OUT -> throw new SquadException(ErrorCode.SQUAD_KICKED_OUT);
            };
        }

        joinSquad(user, squad);

        return Map.of("status", squad.getJoinType().equals(JoinType.APPROVAL) ? "requested" : "approved");

    }

    public DataResponse homeSquad(User user, Pageable pageable, SquadFilterRequest req) {

        List<User> excludedUser = new ArrayList<>();
        if (user != null) {
            excludedUser = getExcludedUsers(user);
        }
        Page<Squad> squads;

        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        if (excludedUser.isEmpty()) {
            squads = squadRepository.findFilteredSquads(req.getCategory(), req.getRegionMain(),
                    req.getRegionSub(), req.isRecruitingOnly(), sevenDaysAgo, pageable);

        } else {
            squads = squadRepository.findFilteredSquadsWithExclusion(req.getCategory(), req.getRegionMain(),
                    req.getRegionSub(), req.isRecruitingOnly(), excludedUser, sevenDaysAgo, pageable);
        }
        return new DataResponse(squads.getContent().stream().map(SquadSimpleDto::toDto).toList(), squads.hasNext());
    }

    public SquadDetailDto detail(User user, long squadId) {
        Squad squad = findSquad(squadId);
        return SquadDetailDto.toDto(user, squad);
    }

    public List<UserSimpleDto> getParticipants(User user, Long squadId) {
        Squad squad = squadRepository.findById(squadId)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));

        if (squad.getJoinType().equals(JoinType.DIRECT)) {
            throw new SquadException(ErrorCode.NOT_APPROVAL_SQUAD);
        }
        if (user.getId() != squad.getUser().getId()) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }
        return squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.PENDING)
                .map(members -> UserSimpleDto.toDto(members.getUser()))
                .collect(Collectors.toList());
    }


    public void closeSquad(User user, Long squadId) {
        Squad squad = findSquad(squadId);

        validateOwner(user, squad);
        squad.setClosed(true);
        squadRepository.save(squad);
    }


    public void joinSquad(User user, Squad squad) {
        validateAlreadyJoined(user, squad);
        validateIsClosed(squad);
        validateTimePassed(squad);
        validateFullJoined(squad);
        validateGender(user ,squad);
        validateAgeRange(user , squad);

        SquadMember squadMember = SquadMember.create(user, squad);
        squadMember = squadMemberRepository.save(squadMember);
        squad.joinParticipant(squadMember);
        squadRepository.save(squad);

        // firebase 알람 처리
        String fcmToken = squad.getUser().getFcmToken();
        boolean feedAlarm = squad.getUser().getFeedAlarm();
        String title = "모임에 새로운 멤버가 참여했습니다.";
        String content = String.format("회원님의 모임글 [%s]에 새로운 멤버가 참여했습니다.",
                squad.getTitle().substring(0, Math.min(20, squad.getTitle().length())));
        String data = squad.getId() + ",squad";
        firebaseMessagingService.sendCommentNotification(feedAlarm, fcmToken, title, content, data);
    }



    public void approveParticipant(User user, Long userId, Long squadId) {
        Squad squad = findSquad(squadId);
        User members = findUser(userId);
        if (!Objects.equals(squad.getUser().getId(), user.getId())) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }
        long joinedCount = squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED).count();
        if (joinedCount >= squad.getMaxParticipants()) {
            throw new SquadException(ErrorCode.SQUAD_FULL);
        }
        SquadMember squadMember = squad.getMembers().stream()
                .filter(m -> m.getUser().equals(members))
                .findAny().orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));
        squadMember.setAttendanceStatus(AttendanceStatus.JOINED);
        squadMemberRepository.save(squadMember);
    }

    public void rejectParticipant(User user, Long userId, Long squadId) {
        Squad squad = findSquad(squadId);
        User members = findUser(userId);
        if (!Objects.equals(squad.getUser().getId(), user.getId())) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }
        SquadMember squadMember = squad.getMembers().stream()
                .filter(m -> m.getUser().equals(members))
                .findAny().orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));
        squadMember.setAttendanceStatus(AttendanceStatus.REJECTED);
        squadMemberRepository.save(squadMember);
    }


    public void kickOffParticipant(User user, Long userId, Long squadId) {
        Squad squad = findSquad(squadId);

        User members = findUser(userId);

        if (squad.getUser().getId() != user.getId()) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }

        SquadMember participant = squad.getMembers().stream().filter(m -> m.getUser() == members)
                .findAny().orElseThrow();
        participant.setAttendanceStatus(AttendanceStatus.KICKED_OUT);
        squad.decreaseCurrentCount();
        squadRepository.save(squad);
        squadMemberRepository.save(participant);
    }

    public void kickOffParticipant(User user, Long squadId) {
        Squad squad = findSquad(squadId);

        SquadMember squadMember = squad.getMembers().stream()
                .filter(m -> m.getUser().getId() == user.getId())
                .findAny().orElseThrow();
        squad.getMembers().remove(squadMember);
        squadMemberRepository.delete(squadMember);
    }









    @Transactional
    public void disbannedSquad(Squad squad) {
        List<SquadMember> members = squadMemberRepository.findAllBySquad(squad);

        squadMemberRepository.deleteAll(members);


        squadRepository.delete(squad);
    }




//
//
//    public boolean isAgeInRange(int userAge, Age age) {
//        return switch (age) {
//            case TEEN -> userAge >= 10 && userAge < 20;e
//            case TWENTIES -> userAge >= 20 && userAge < 30;
//            case THIRTIES -> userAge >= 30 && userAge < 40;
//            case FORTIES -> userAge >= 40 && userAge < 50;
//            case FIFTIES -> userAge >= 50;
//            case ALL -> true; // 연령 무관
//        };
//    }





    private List<User> getExcludedUsers(User currentUser) {
        List<User> blockedUsers = new ArrayList<>(blockRepository.findByBlocker(currentUser).stream()
                .map(block -> block.getBlocked())
                .toList());

        List<User> blockedByUsers = blockRepository.findByBlocked(currentUser).stream()
                .map(block -> block.getBlocker())
                .toList();

        blockedUsers.addAll(blockedByUsers);
        return blockedUsers.stream().distinct().toList();

    }

}









