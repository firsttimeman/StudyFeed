package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SquadValidator {

    private final SquadRepository squadRepository;
    private final SquadMemberRepository squadMemberRepository;
    private final BlockRepository blockRepository;

    public void validateOwner(User user, Squad squad) {
        if (!Objects.equals(user.getId(), squad.getUser().getId())) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }
    }

    public void validateAgeConflictOnUpdate(Long squadId, Long ownerId, int newMinAge, int newMaxAge) {
        if (newMinAge < 20 || newMinAge > newMaxAge) {
            throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
        }

        LocalDate today = LocalDate.now();
        LocalDate minBirth = today.minusYears(newMaxAge);
        LocalDate maxBirth = today.minusYears(newMinAge);

        if (squadMemberRepository.existsJoinedOutOfAge(
                squadId,
                ownerId,
                MembershipStatus.JOINED,
                minBirth,
                maxBirth
        )) {
            throw new SquadException(ErrorCode.SQUAD_AGE_CONFLICT);
        }
    }

    public void validateGenderConflictOnUpdate(Long squadId, Gender required) {
        if (required == Gender.ALL) {
            return;
        }

        boolean conflict = squadMemberRepository.existsJoinedGenderConflict(
                squadId,
                MembershipStatus.JOINED,
                required
        );

        if (conflict) {
            throw new SquadException(ErrorCode.SQUAD_GENDER_CONFLICT);
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

    public void validateDeleteMember(Squad squad, boolean isForcedDelete) {
        if (!isForcedDelete && !squad.isOnlyOneLeft()) {
            throw new SquadException(ErrorCode.SQUAD_DELETE_NOT_ALLOWED);
        }
    }

    public void validateGenderEligibility(User user, Squad squad) {
        Gender required = squad.getGenderRequirement();
        if (!matchesGenderRequirement(user, required)) {
            throw new SquadException(ErrorCode.GENDER_NOT_ALLOWED);
        }
    }

    public void validateAgeEligibility(User user, Squad squad) {
        int age = user.getAge();
        int min = squad.getMinAge();
        int max = squad.getMaxAge();

        if (age < min || age > max) {
            throw new SquadException(ErrorCode.AGE_NOT_ALLOWED);
        }
    }

    private boolean matchesGenderRequirement(User user, Gender required) {
        if (required == Gender.ALL) return true;
        Gender userGender = user.getGender();
        return required == userGender;
    }
}
