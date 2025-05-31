package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.RecruitStatus;
import FeedStudy.StudyFeed.squad.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.squad.dto.SquadJoinResponse;
import FeedStudy.StudyFeed.squad.dto.SquadUpdateRequestDto;
import FeedStudy.StudyFeed.global.service.RegionService;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.squad.entity.SquadReport;
import FeedStudy.StudyFeed.squad.util.ChatTokenProvider;
import FeedStudy.StudyFeed.user.entity.User;

import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadReportRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.global.type.SquadAccessType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SquadService extends AbstractSquadService {


    private final ChatTokenProvider chatTokenProvider;

    public SquadService(SquadRepository squadRepository, UserRepository userRepository, SquadMemberRepository squadMemberRepository, RegionService regionService, SquadReportRepository squadReportRepository, ChatTokenProvider chatTokenProvider) {
        super(squadRepository, userRepository, squadMemberRepository, regionService, squadReportRepository);
        this.chatTokenProvider = chatTokenProvider;
    }

    @Transactional
    @Override
    public void createSquad(SquadCreateRequestDto requestDto, User user) {

        User leader = findUserById(user.getId());

        validateAgeRestriction(requestDto);

        Squad squad = Squad.build(requestDto, leader);

        squadRepository.save(squad);

        SquadMember squadMember = SquadMember.createSquadMember(squad, leader);

        squadMemberRepository.save(squadMember);

    }

    @Override
    public void updateSquad(Long squadId, User user, SquadUpdateRequestDto requestDto) {

        Squad squad = findSquadById(squadId);


        validateSquadLeader(squad, user);

        regionService.checkRegion(requestDto);

        squad.update(requestDto);

    }

    @Transactional
    @Override

    public void deleteSquad(Long squadId, User user) {

        Squad squad = findSquadById(squadId);

        validateSquadLeader(squad, user);

        squadRepository.delete(squad);
    }

    @Transactional // todo 더 줄일수 있는지 시도해보기
    @Override

    public void joinSquad(Long squadId, User newUser) {
        Squad squad = findSquadById(squadId);

        IsBannedChecked(squad, newUser);
        validateBySquadAndUser(squad, newUser);
        validatePeopleNum(squad);
        validateGender(squad, newUser);

        int userAge = newUser.getAge();

        validateMinAge(squad, userAge);
        validateMaxAge(squad, userAge);

        SquadMember squadMember = SquadMember.createSquadMember(squad, newUser);
        squadMemberRepository.save(squadMember);

//        if (squad.getSquadAccessType() == SquadAccessType.OPEN) {
//            squad.setCurrentPeopleNum(squad.getCurrentPeopleNum() + 1);
//            squadRepository.save(squad);
//        }

        if (squad.getSquadAccessType() == SquadAccessType.APPROVAL) {
            squadMember.setAttendanceStatus(AttendanceStatus.PENDING);
        } else {
            squadMember.setAttendanceStatus(AttendanceStatus.APPROVED);
            squad.setCurrentPeopleNum(squad.getCurrentPeopleNum() + 1);
            squadRepository.save(squad);
        }

    }

    @Transactional
    @Override

    public void approveMember(Long squadMemberId, User user) {
        SquadMember squadMember = findSquadMemberById(squadMemberId);

        Squad squad = squadMember.getSquad();

        validateSquadLeader(squad, user);
        validateStatus(squadMember);
        validatePeopleNum(squad);

        squadMember.setAttendanceStatus(AttendanceStatus.APPROVED);
        squadMemberRepository.save(squadMember);

        squad.setCurrentPeopleNum(squad.getCurrentPeopleNum() + 1);
        squadRepository.save(squad);
    }

    @Transactional
    @Override
    public void rejectMember(Long squadMemberId, User user) {
        SquadMember squadMember = findSquadMemberById(squadMemberId);
        Squad squad = squadMember.getSquad();

        validateSquadLeader(squad, user);
        validateTargetIsLeader(squad, squadMember);

        squadMember.rejected();

        squadMemberRepository.save(squadMember);
    }


    @Transactional
    @Override

    public void closeRecruitment(Long squadId, User user) {
        Squad squad = findSquadById(squadId);

        validateSquadLeader(squad, user);
        validateCheckClosed(squad);

        squad.setRecruitStatus(RecruitStatus.CLOSED);
        squad.setPeopleNum(squad.getCurrentPeopleNum());

        squadRepository.save(squad);
    }


    @Transactional
    @Override
    public void kickMember(Long squadId, User leader, Long targetId) {
        Squad squad = findSquadById(squadId);

        validateSquadLeader(squad, leader);

        User user = findUserById(targetId);

        SquadMember squadMember = validateUserInSquad(squad, user);

        validateBannedUser(squadMember);

        squadMember.setAttendanceStatus(AttendanceStatus.BANNED);
        squadMemberRepository.save(squadMember);

        squad.decreasingCurrentPeopleNum();
        squadRepository.save(squad);
    }

    @Transactional
    @Override

    public void leaveSquad(Long squadId, User user) {

        Squad squad = findSquadById(squadId);

        SquadMember squadMember = validateUserInSquad(squad, user);

        if (squad.getLeader().equals(user)) {
            disbannedSquad(squad);
            return;
        }

        squadMember.setAttendanceStatus(AttendanceStatus.WITHDRAW);
        squadMemberRepository.save(squadMember);

        squad.setCurrentPeopleNum(squad.getCurrentPeopleNum() - 1);

        squadRepository.save(squad);
    }

    @Transactional
    @Override
    public void reportMember(Long squadId, User reporter, Long reportedId, String reason) {
        Squad squad = findSquadById(squadId);
        User reportedUser = findUserById(reportedId);

        validateReporter(reporter, reportedUser);
        validateReporterAndUserAndSquad(reporter, reportedUser, squad);

        SquadReport report = SquadReport.createSquadReport(reporter, reportedUser, squad, reason);
        squadReportRepository.save(report);

        long reportCount = squadReportRepository.countBySquadAndReportedUser(squad, reportedUser);
        if (reportCount >= 3) {
            kickMember(squadId, reporter, reportedId);
        }

    }


    @Transactional
    public void disbannedSquad(Squad squad) {
        List<SquadMember> members = squadMemberRepository.findAllBySquad(squad);

        squadMemberRepository.deleteAll(members);


        squadRepository.delete(squad);
    }

    public SquadJoinResponse joinOrGetChatToken(Long squadId, User user) {
        Squad squad = findSquadById(squadId);

        Optional<SquadMember> participantOpt = squadMemberRepository.findBySquadAndUser(squad, user);

        if(participantOpt.isPresent()) {
            SquadMember squadMember = participantOpt.get();
            validateBannedUser(squadMember);

            boolean isFirstChat = !squadMember.isChatEntered();

            if(isFirstChat) {
                squadMember.setChatEntered(true);
                squadMemberRepository.save(squadMember);
            }

            String chatToken = chatTokenProvider.createChatToken(user, squad);
            return new SquadJoinResponse(isFirstChat ? "requested" : "joined", chatToken);

        }

        joinSquad(squadId, user);

        SquadMember joinedMember = squadMemberRepository.findBySquadAndUser(squad, user)
                .orElseThrow(() -> new IllegalArgumentException("참여 정보 저장 실패"));

        if(joinedMember.getAttendanceStatus() == AttendanceStatus.PENDING) {
            return new SquadJoinResponse("requested", null); // 승인대기
        } else {
            String chatToken = chatTokenProvider.createChatToken(user, squad);
            return new SquadJoinResponse("joined", null); // 즉시 참여 join
        }

    }


//
//
//    public boolean isAgeInRange(int userAge, Age age) {
//        return switch (age) {
//            case TEEN -> userAge >= 10 && userAge < 20;
//            case TWENTIES -> userAge >= 20 && userAge < 30;
//            case THIRTIES -> userAge >= 30 && userAge < 40;
//            case FORTIES -> userAge >= 40 && userAge < 50;
//            case FIFTIES -> userAge >= 50;
//            case ALL -> true; // 연령 무관
//        };
//    }

    private boolean IsBannedChecked(Squad squad, User user) {

        if (squadMemberRepository.existsBySquadAndUserAndAttendanceStatus(squad, user, AttendanceStatus.BANNED)) {
            throw new IllegalArgumentException("이미 강퇴된 유저입니다.");
        }

        return true;
    }


}









