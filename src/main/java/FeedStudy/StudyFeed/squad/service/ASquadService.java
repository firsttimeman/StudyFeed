package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.squad.dto.SquadRequest;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
public abstract class ASquadService {

    protected final SquadRepository squadRepository;
    protected final UserRepository userRepository;
    protected final SquadMemberRepository squadMemberRepository;
    protected final FirebaseMessagingService firebaseMessagingService;
    protected final BlockRepository blockRepository;

    public Squad findSquad(Long squadId) {
        return squadRepository.findById(squadId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임을 찾을 수 없습니다."));
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
    }

    public void validateOwner(User user, Squad squad) {
        if (user.getId() != squad.getUser().getId()) {
            throw new IllegalArgumentException("사용자가 아닙니다.");
        }
    }

    public void validateAgeRange(Squad squad, SquadRequest req) {
        int minAge = req.getMinAge();
        int maxAge = req.getMaxAge();
        if (squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED
                        && member.getUser().getId() != squad.getUser().getId())
                .anyMatch(member -> isAgeValidate(minAge, maxAge, member.getUser().getAge()))) {
            throw new IllegalArgumentException("이미 참여 중인 다른 나이대의 멤버가 있어서 수정이 어려워요.");

        }
    }


    public void validateAgeRange(User user, Squad squad) {
        int minAge = squad.getMinAge();
        int maxAge = squad.getMaxAge();
        int age = user.getAge();
        if(isAgeValidate(minAge, maxAge, age)) {
            throw new IllegalArgumentException("참여 가능한 연령이 아닙니다.");
        }

    }

    public void validateGender(Squad squad, SquadRequest req) {

        if(squad.getMembers().stream()
                .anyMatch(member -> isGenderValidate(member.getUser(), req.getGenderRequirement()))) {
            throw new IllegalArgumentException("이미 참여 중인 다른 성별의 멤버가 있어서 수정이 어렵습니다.");

        }
    }

    public void validateGender(User user, Squad squad) {
        if(isGenderValidate(user, squad.getGenderRequirement())) {
            throw new IllegalArgumentException("참여 가능한 성별이 아닙니다.");
        }
    }

    public void validateMemberCount(Squad squad, SquadRequest req) {
        int joinedCount = (int) squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED)
                .count();

        if(joinedCount > req.getMaxParticipants()) {
            throw new IllegalArgumentException(String.format("이미 %d명이 참여중이라 인원 수정이 어렵습니다.", joinedCount));
        }
    }


    public void validateDeleteMember(Squad squad, boolean isForcedDelete) {
        if (!isForcedDelete && !squad.isOnlyOneLeft()) {
            throw new IllegalArgumentException("멤버를 모두 내보낸 다음 삭제할 수 있어요. 멤버 닉네임 옆의 '관리'를 눌러주세요");
        }
    }

    public void validateAlreadyJoined(User user, Squad squad) {
        if(squad.getMembers().stream().anyMatch(member -> member.getUser() == user)) {
            throw new IllegalArgumentException("이미 참여중인 사용자입니다.");
        }
    }

    public void validateIsClosed(Squad squad) {
        if(squad.isClosed()) {
            throw new IllegalArgumentException("마감된 모임입니다.");
        }
    }

    public void validateTimePassed(Squad squad) {
        if(LocalDateTime.of(squad.getDate(), squad.getTime() == null ? LocalTime.of(23, 59, 59) : squad.getTime())
                .isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("종료된 모임입니다.");
        }
    }

    public void validateFullJoined(Squad squad) {
        if(squad.getMembers().stream()
                .filter(m -> m.getAttendanceStatus() == AttendanceStatus.JOINED)
                .count() >= squad.getMaxParticipants()) {
            throw new IllegalArgumentException("참여 인원이 찼습니다.");
        }
    }


    public boolean isGenderValidate(User user, Gender gender) {
        return gender == Gender.FEMALE && user.getGender().equals("여성")
                || gender == Gender.MALE && user.getGender().equals("남성");
    }



    public boolean isAgeValidate(int minAge, int maxAge, int age) {
        return minAge > age || maxAge < age;
    }


}
