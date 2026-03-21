    package FeedStudy.StudyFeed.squad.service;

    import FeedStudy.StudyFeed.global.config.DistributeLock;
    import FeedStudy.StudyFeed.global.exception.ErrorCode;
    import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
    import FeedStudy.StudyFeed.global.service.RegionService;
    import FeedStudy.StudyFeed.global.type.Gender;
    import FeedStudy.StudyFeed.squad.dto.SquadCreateResponse;
    import FeedStudy.StudyFeed.squad.dto.SquadRequest;
    import FeedStudy.StudyFeed.squad.dto.UpdateSquadRequest;
    import FeedStudy.StudyFeed.squad.entity.Squad;
    import FeedStudy.StudyFeed.squad.entity.SquadMember;
    import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
    import FeedStudy.StudyFeed.squad.repository.SquadRepository;
    import FeedStudy.StudyFeed.user.entity.User;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.Objects;
    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    public class SquadService  {


        private final SquadRepository squadRepository;
        private final SquadMemberRepository squadMemberRepository;
        private final RegionService regionService;
        private final SquadFinder squadFinder;
        private final SquadValidator squadValidator;

        private static final int MIN_ALLOWED_AGE = 20;

        @Transactional
        public SquadCreateResponse createSquad(SquadRequest req, User user) {

            regionService.checkRegion(req.getRegionMain(), req.getRegionSub());

            if (req.getMaxParticipants() < 1) {
                throw new SquadException(ErrorCode.SQUAD_MEMBER_COUNT_EXCEEDED);
            }


            if (req.getMinAge() < MIN_ALLOWED_AGE) {
                throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
            }

            if (req.getMinAge() > req.getMaxAge()) {
                throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
            }

            if (Boolean.TRUE.equals(req.getTimeSpecified()) && req.getTime() == null) {
                throw new SquadException(ErrorCode.TIME_REQUIRED_WHEN_SPECIFIED);
            }

            if (req.getDate().isBefore(LocalDate.now())) {
                throw new SquadException(ErrorCode.SQUAD_TIME_PASSED);
            }

            if (user.getAge() < MIN_ALLOWED_AGE) {
                throw new SquadException(ErrorCode.AGE_NOT_ALLOWED);
            }

            Squad squad = squadRepository.save(Squad.create(user, req));
            squadMemberRepository.save(SquadMember.createOwner(user, squad));

            return new SquadCreateResponse("created", squad.getId());
        }


        @DistributeLock(keyPrefix = "squad:", argIndex = 0, waitTime = 3, leaseTime = 10)
        @Transactional
        public Squad updateSquad(Long squadId, User user, UpdateSquadRequest req) {
            Squad squad = squadFinder.findSquad(squadId);
            squadValidator.validateOwner(user, squad);

            int newMinAge = Optional.ofNullable(req.getMinAge()).orElse(squad.getMinAge());
            int newMaxAge = Optional.ofNullable(req.getMaxAge()).orElse(squad.getMaxAge());
            int newMaxParticipants = Optional.ofNullable(req.getMaxParticipants()).orElse(squad.getMaxParticipants());
            Gender newGender = Optional.ofNullable(req.getGenderRequirement()).orElse(squad.getGenderRequirement());
            LocalDate newDate = Optional.ofNullable(req.getDate()).orElse(squad.getDate());
            boolean newTimeSpecified = Optional.ofNullable(req.getTimeSpecified()).orElse(squad.isTimeSpecified());
            LocalTime newTime = newTimeSpecified
                    ? Optional.ofNullable(req.getTime()).orElse(squad.getTime())
                    : null;
            String newRegionMain = Optional.ofNullable(req.getRegionMain()).orElse(squad.getRegionMain());
            String newRegionSub = Optional.ofNullable(req.getRegionSub()).orElse(squad.getRegionSub());

            if (newMinAge < MIN_ALLOWED_AGE || newMinAge > newMaxAge) {
                throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
            }
            if (newTimeSpecified && newTime == null) {
                throw new SquadException(ErrorCode.TIME_REQUIRED_WHEN_SPECIFIED);
            }
            if (newDate.isBefore(LocalDate.now())) {
                throw new SquadException(ErrorCode.SQUAD_TIME_PASSED);
            }
            if (newMaxParticipants < 1) {
                throw new SquadException(ErrorCode.SQUAD_MEMBER_COUNT_EXCEEDED);
            }

            if (!Objects.equals(squad.getRegionMain(), newRegionMain)
                || !Objects.equals(squad.getRegionSub(), newRegionSub)) {
                regionService.checkRegion(newRegionMain, newRegionSub);
            }

            squadValidator.validateAgeConflictOnUpdate(
                    squad.getId(),
                    squad.getUser().getId(),
                    newMinAge,
                    newMaxAge
            );
            squadValidator.validateGenderConflictOnUpdate(squad.getId(), newGender);

            if (newMaxParticipants != squad.getMaxParticipants()) {
                int rows = squadRepository.tryUpdateMaxParticipants(squadId, newMaxParticipants);
                if (rows == 0) {
                    throw new SquadException(ErrorCode.SQUAD_MEMBER_COUNT_EXCEEDED);
                }
                squad.changeMaxParticipants(newMaxParticipants);
            }

            squad.updateExceptCapacity(req);
            refreshClosedByCapacity(squad);

            return squad;
        }

        @DistributeLock(keyPrefix = "squad:", argIndex = 0, waitTime = 3, leaseTime = 10)
        @Transactional
        public void deleteSquad(Long squadId, User user, boolean isForcedDelete) {
            Squad squad = squadFinder.findSquad(squadId);
            squadValidator.validateOwner(user, squad);
            squadValidator.validateDeleteMember(squad, isForcedDelete);

            squadRepository.delete(squad);
        }

        @DistributeLock(keyPrefix = "squad:", argIndex = 1, waitTime = 3, leaseTime = 10)
        @Transactional
        public void closeSquad(User user, Long squadId) {
            Squad squad = squadFinder.findSquad(squadId);
            squadValidator.validateOwner(user, squad);

            if (squad.isClosed()) {
                throw new SquadException(ErrorCode.SQUAD_ALREADY_CLOSED);
            }

            squad.close();
        }




        private void refreshClosedByCapacity(Squad squad) {
            if (squad.getCurrentCount() >= squad.getMaxParticipants()) {
                squadRepository.closeIfFull(squad.getId());
                squad.close();
            } else {
                squadRepository.openIfNotFull(squad.getId());
                squad.open();
            }
        }
    }
