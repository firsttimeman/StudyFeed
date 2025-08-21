package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
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
import org.springframework.security.access.AccessDeniedException;

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
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateOwner(User user, Squad squad) {
        if (user.getId() != squad.getUser().getId()) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }
    }

    public void validateAgeRange(Squad squad, SquadRequest req) {
        int minAge = req.getMinAge();
        int maxAge = req.getMaxAge();
        boolean hasInvalid = squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED
                                  && !member.getUser().getId().equals(squad.getUser().getId()))
                .anyMatch(member -> isAgeValidate(minAge, maxAge, member.getUser().getAge())); // todo n+1 멤버수 만큼 발생
        if (hasInvalid) {
            throw new SquadException(ErrorCode.SQUAD_AGE_CONFLICT);
        }
    }



    public void validateAgeRange(User user, Squad squad) {
        int minAge = squad.getMinAge();
        int maxAge = squad.getMaxAge();
        int age = user.getAge();
        if(isAgeValidate(minAge, maxAge, age)) {
            throw new SquadException(ErrorCode.AGE_NOT_ALLOWED);
        }

    }

    public void validateGender(Squad squad, SquadRequest req) {
        boolean hasInvalid = squad.getMembers().stream() // todo  멤버 수만큼 LAZY 로딩 → N+1
                .anyMatch(member -> isGenderValidate(member.getUser(), req.getGenderRequirement()));
        if (hasInvalid) {
            throw new SquadException(ErrorCode.SQUAD_GENDER_CONFLICT);
        }
    }

    public void validateGender(User user, Squad squad) {
        if(isGenderValidate(user, squad.getGenderRequirement())) {
            throw new SquadException(ErrorCode.GENDER_NOT_ALLOWED);
        }
    }

    public void validateMemberCount(Squad squad, SquadRequest req) {
        int joinedCount = (int) squad.getMembers().stream()
                .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED)
                .count();

        if(joinedCount > req.getMaxParticipants()) {
            throw new SquadException(ErrorCode.SQUAD_MEMBER_COUNT_EXCEEDED);
        }
    }


    public void validateDeleteMember(Squad squad, boolean isForcedDelete) {
        if (!isForcedDelete && !squad.isOnlyOneLeft()) {
            throw new SquadException(ErrorCode.SQUAD_DELETE_NOT_ALLOWED);
        }
    }

    public void validateAlreadyJoined(User user, Squad squad) {
        boolean alreadyJoined = squad.getMembers().stream() // todo n+1 발생 가능
                .anyMatch(member -> member.getUser().equals(user));
        if (alreadyJoined) {
            throw new SquadException(ErrorCode.ALREADY_JOINED);
        }
    }

    public void validateIsClosed(Squad squad) {
        if (squad.isClosed()) {
            throw new SquadException(ErrorCode.SQUAD_CLOSED);
        }
    }



    public void validateTimePassed(Squad squad) {
        LocalDateTime endTime = LocalDateTime.of(
                squad.getDate(),
                squad.getTime() == null ? LocalTime.of(23, 59, 59) : squad.getTime()
        );
        if (endTime.isBefore(LocalDateTime.now())) {
            throw new SquadException(ErrorCode.SQUAD_TIME_PASSED);
        }
    }

    public void validateFullJoined(Squad squad) {
        long joined = squad.getMembers().stream()
                .filter(m -> m.getAttendanceStatus() == AttendanceStatus.JOINED)
                .count();
        if (joined >= squad.getMaxParticipants()) {
            throw new SquadException(ErrorCode.SQUAD_FULL);
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
