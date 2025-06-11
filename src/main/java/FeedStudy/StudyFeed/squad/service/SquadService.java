package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.SquadAccessType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public void createSquad(SquadRequest req, User user) {

        Squad squad = Squad.create(user ,req);
        squad = squadRepository.save(squad);

        SquadMember squadMember = SquadMember.create(user, squad);
        squadMemberRepository.save(squadMember);

    }

    public void updateSquad(Long squadId, User user, SquadRequest req) {
        Squad squad = findSquad(squadId);

        validateOwner(user, squad);

        validateAgeRange(squad, req);

        validateGender(squad, req);

        validateMemberCount(squad, req);

        squad.update(req);

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
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다"));

        Optional<SquadMember> members = squadMemberRepository.findBySquadAndUser(squad, user);

        if(members.isPresent()) {
            SquadMember squadMember = members.get();

            if(squadMember.getAttendanceStatus() == AttendanceStatus.JOINED) {
                String chatToken = chatTokenProvider.createChatToken(user, squad);
                return Map.of("status", "joined", "chatToken", chatToken);
            }

            if(squadMember.getAttendanceStatus() == AttendanceStatus.PENDING) {
                return Map.of("status", "pending");
            }

            if(squadMember.getAttendanceStatus() == AttendanceStatus.REJECTED) {
                throw new IllegalArgumentException("참여가 거절된 모임입니다.");
            }

            if(squadMember.getAttendanceStatus() == AttendanceStatus.KICKED_OUT) {
                throw new IllegalArgumentException("강제 퇴장된 유저입니다.");
            }
        }

        joinSquad(user, squad);

        return Map.of("status", squad.getSquadAccessType().equals(SquadAccessType.APPROVAL) ? "requested" : "approved");
    }



    public DataResponse homeSquad(User user, Pageable pageable, SquadFilterRequest req) {

        List<User> excludedUser = new ArrayList<>();
        if(user != null) {
            excludedUser = getExcludedUsers(user);
        }
        Page<Squad> squads;

        if(excludedUser.isEmpty()) {
          squads = squadRepository.findFilteredSquads(req.getCategory(), req.getRegionMain(),
                    req.getRegionSub(), req.isRecruitingOnly(), pageable);

        } else {
            squads = squadRepository.findFilteredSquadsWithExclusion(req.getCategory(), req.getRegionMain(),
                    req.getRegionSub(), req.isRecruitingOnly(), excludedUser, pageable);
        }
        return new DataResponse(squads.getContent().stream().map(SquadSimpleDto::toDto).toList(), squads.hasNext());
    }

    public SquadDetailDto detail(User user, long squadId) {
        Squad squad = findSquad(squadId);
        return SquadDetailDto.toDto(user, squad);
    }

    public List<UserSimpleDto> getParticipants(User user, Long squadId) {
        Squad squad = squadRepository.findById(squadId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임을 찾을 수 없습니다."));

        if (squad.getSquadAccessType().equals(SquadAccessType.DIRECT)) {
            throw new AccessDeniedException("승인제가 아닙니다.");
        }
        if (user.getId() != squad.getUser().getId()) {
            throw new IllegalArgumentException("스쿼드 장이 아닙니다.");
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

        if (squad.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("방장이 아닙니다. 승인 권한이 없습니다.");
        }

        if(squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED)
                .count() >= squad.getMaxParticipants()) {
            throw new IllegalArgumentException("참여 인원이 찼습니다.");
        }

        SquadMember squadMember = squad.getMembers().stream().filter(m -> m.getUser() == members)
                .findAny().orElseThrow();
        squadMember.setAttendanceStatus(AttendanceStatus.JOINED);
        squadMemberRepository.save(squadMember);

    }

    public void rejectParticipant(User user, Long userId, Long squadId) {
        Squad squad = findSquad(squadId);

        User members = findUser(userId);

        if (squad.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("방장이 아닙니다. 승인 권한이 없습니다.");
        }

        SquadMember squadMember = squad.getMembers().stream().filter(m -> m.getUser() == members)
                .findAny().orElseThrow();
        squadMember.setAttendanceStatus(AttendanceStatus.REJECTED);
        squadMemberRepository.save(squadMember);
    }

    public void kickOffParticipant(User user, Long userId, Long squadId) {
        Squad squad = findSquad(squadId);

        User members = findUser(userId);

        if (squad.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("방장이 아닙니다. 승인 권한이 없습니다.");
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









