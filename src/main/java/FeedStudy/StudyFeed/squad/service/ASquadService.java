package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@RequiredArgsConstructor
public abstract class ASquadService {

    protected final SquadRepository squadRepository;
    protected final UserRepository userRepository;
    protected final SquadMemberRepository squadMemberRepository;
    protected final FirebaseMessagingService firebaseMessagingService;
    protected final BlockRepository blockRepository;

    // ===== 조회 유틸 =====
    public Squad findSquad(Long squadId) {
        return squadRepository.findById(squadId)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateOwner(User user, Squad squad) {
        if (!Objects.equals(user.getId(), squad.getUser().getId())) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }
    }

    // ===== 업데이트 시 검증 (⚠️ SquadService 쪽 호출 시그니처에 맞춤) =====

    /**
     * 나이 범위 변경 시, 이미 JOINED 중 범위 밖 사용자가 존재하면 거부(리더 제외)
     */
    protected void validateAgeConflictOnUpdate(Long squadId, Long ownerId, int newMinAge, int newMaxAge) {
        if (newMinAge < 0 || newMinAge > newMaxAge) {
            throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
        }
        LocalDate today = LocalDate.now();
        LocalDate minBirth = today.minusYears(newMaxAge); // 이보다 이전이면 초과(나이 많음)
        LocalDate maxBirth = today.minusYears(newMinAge); // 이보다 이후면 미달(나이 적음)

        boolean conflict = squadMemberRepository.existsJoinedOutOfAge(
                squadId, ownerId, minBirth, maxBirth
        );
        if (conflict) throw new SquadException(ErrorCode.SQUAD_AGE_CONFLICT);
    }

    /**
     * 성별 조건 변경 시, 이미 JOINED 중 조건 불만족 사용자가 존재하면 거부
     */
    protected void validateGenderConflictOnUpdate(Long squadId, Gender required) {
        String requiredEnumName = required.name(); // "ALL" | "MALE" | "FEMALE"
        String requiredKorName =
                requiredEnumName.equals("MALE") ? "남성" :
                        requiredEnumName.equals("FEMALE") ? "여성" : "ALL";

        boolean conflict = squadMemberRepository.existsJoinedGenderConflict(
                squadId, requiredEnumName, requiredKorName
        );
        if (conflict) throw new SquadException(ErrorCode.SQUAD_GENDER_CONFLICT);
    }

    /**
     * 정원 변경 시, 현재 JOINED 인원이 새 maxParticipants 를 초과하면 거부
     */




    public void validateIsClosed(Squad squad) {
        if (squad.isClosed()) throw new SquadException(ErrorCode.SQUAD_CLOSED);
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



    // ===== 삭제 검증 =====
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
        if (isAgeNotInRange(min, max, age)) {
            throw new SquadException(ErrorCode.AGE_NOT_ALLOWED);
        }
    }


    // ===== 헬퍼 =====

    /**
     * Gender 요구사항과 User.gender("남성"/"여성") 일치 여부
     */
    protected boolean matchesGenderRequirement(User user, Gender required) {
        if (required == Gender.ALL) return true;
        String g = user.getGender(); // 현재 프로젝트에서 문자열 사용 ("남성"/"여성")
        return (required == Gender.MALE && "남성".equals(g))
               || (required == Gender.FEMALE && "여성".equals(g));
    }

    /**
     * 나이 범위 밖이면 true
     */
    protected boolean isAgeNotInRange(int minAge, int maxAge, int age) {
        return age < minAge || age > maxAge;
    }

    /**
     * 정원 기반으로 closed 플래그 동기화 (새 maxParticipants를 전달받아 사용)
     */
    protected void refreshClosedByCapacity(Squad squad) {
        if (squad.getCurrentCount() >= squad.getMaxParticipants()) {
            squadRepository.closeIfFull(squad.getId());
            squad.close();     // 엔티티 동기화
        } else {
            squadRepository.openIfNotFull(squad.getId());
            squad.open();      // 엔티티 동기화

        }

    }

}