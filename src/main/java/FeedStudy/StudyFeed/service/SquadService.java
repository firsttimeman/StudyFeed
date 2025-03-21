package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.dto.SquadUpdateRequestDto;
import FeedStudy.StudyFeed.entity.Squad.Squad;
import FeedStudy.StudyFeed.entity.Squad.SquadMember;
import FeedStudy.StudyFeed.entity.Squad.SquadReport;
import FeedStudy.StudyFeed.entity.User;

import FeedStudy.StudyFeed.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.repository.SquadReportRepository;
import FeedStudy.StudyFeed.repository.SquadRepository;
import FeedStudy.StudyFeed.repository.UserRepository;
import FeedStudy.StudyFeed.type.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SquadService extends AbstractSquadService {


    public SquadService(SquadRepository squadRepository, UserRepository userRepository, SquadMemberRepository squadMemberRepository, RegionService regionService, SquadReportRepository squadReportRepository) {
        super(squadRepository, userRepository, squadMemberRepository, regionService, squadReportRepository);

    }

    @Transactional
    @Override
    public void createSquad(SquadCreateRequestDto requestDto, User user) {

        User leader = findByEmail(user.getEmail());

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

        if (squad.getSquadAccessType() == SquadAccessType.OPEN) {
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


        String email = user.getEmail();


        Squad squad = findSquadById(squadId);

        findByEmail(email);


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









