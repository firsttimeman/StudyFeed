package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.global.config.DistributeLock;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.squad.dto.PendingApplicantDto;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SquadMembershipService {

    private final SquadRepository squadRepository;
    private final SquadMemberRepository squadMemberRepository;
    private final SquadFinder squadFinder;
    private final SquadValidator squadValidator;


    @DistributeLock(keyPrefix = "squad:", argIndex = 1, waitTime = 3, leaseTime = 5)
    @Transactional
    public Map<String, String> joinSquad(User user, Long squadId) {

        Squad squad = squadFinder.findSquad(squadId);
        squadValidator.validateIsClosed(squad);
        squadValidator.validateTimePassed(squad);
        squadValidator.validateGenderEligibility(user, squad);
        squadValidator.validateAgeEligibility(user, squad);

        Optional<SquadMember> existing = squadMemberRepository.findBySquadAndUser(squad, user);
        if (existing.isPresent()) {
            MembershipStatus status = existing.get().getMembershipStatus();

            switch (status) {
                case JOINED:
                    return Map.of("status", "joined");

                case PENDING:
                    return Map.of("status", "pending");

                case REJECTED:
                    throw new SquadException(ErrorCode.SQUAD_REJECTED);

                case KICKED_OUT:
                    throw new SquadException(ErrorCode.SQUAD_KICKED_OUT);

                case LEFT:
                    if (squad.getJoinType() == JoinType.DIRECT) {
                        int updated = squadRepository.tryIncreaseCount(squadId);
                        if (updated == 0) {
                            throw new SquadException(ErrorCode.SQUAD_FULL);
                        }

                        int changed = squadMemberRepository.rejoinIfLeft(squadId, user.getId());
                        if (changed == 0) {
                            throw new SquadException(ErrorCode.ALREADY_JOINED);
                        }

                        squadRepository.closeIfFull(squadId);
                        return Map.of("status", "approved");
                    }

                    int changed = squadMemberRepository.requestAgainIfLeft(squadId, user.getId());
                    if (changed == 0) {
                        throw new SquadException(ErrorCode.ALREADY_JOINED);
                    }

                    return Map.of("status", "requested");
            }
        }

        if (squad.getJoinType() == JoinType.DIRECT) {
            int updated = squadRepository.tryIncreaseCount(squadId);
            if (updated == 0) {
                throw new SquadException(ErrorCode.SQUAD_FULL);
            }

            try {
                squadMemberRepository.save(SquadMember.createJoined(user, squad));
            } catch (DataIntegrityViolationException e) {
                throw new SquadException(ErrorCode.ALREADY_JOINED);
            }

            squadRepository.closeIfFull(squadId);
            return Map.of("status", "approved");
        }

        try {
            squadMemberRepository.save(SquadMember.createPending(user, squad));
        } catch (DataIntegrityViolationException e) {
            throw new SquadException(ErrorCode.ALREADY_JOINED);
        }

        return Map.of("status", "requested");
    }

    @Transactional
    public List<PendingApplicantDto> getPendingApplicants(User owner, Long squadId) {
        Squad squad = squadFinder.findSquad(squadId);

        if (squad.getJoinType() == JoinType.DIRECT) {
            throw new SquadException(ErrorCode.NOT_APPROVAL_SQUAD);
        }
        squadValidator.validateOwner(owner, squad);
        return squadMemberRepository.findPendingApplicants(squadId);
    }


    @DistributeLock(keyPrefix = "squad:", argIndex = 2, waitTime = 3, leaseTime = 10)
    @Transactional
    public void approveParticipant(User owner, Long targetUserId, Long squadId) {

        Squad squad = squadFinder.findSquad(squadId);
        squadValidator.validateOwner(owner, squad);

        if (squad.getJoinType() == JoinType.DIRECT) {
            throw new SquadException(ErrorCode.NOT_APPROVAL_SQUAD);
        }

        squadValidator.validateTimePassed(squad);

        User target = squadFinder.findUser(targetUserId);

        SquadMember member = squadMemberRepository.findBySquadAndUser(squad, target)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));

        if (member.isOwner()) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }

        if (member.getMembershipStatus() != MembershipStatus.PENDING) {
            throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
        }


        int increased = squadRepository.tryIncreaseCount(squadId);
        if (increased == 0) {
            throw new SquadException(ErrorCode.SQUAD_FULL);
        }

        int updated = squadMemberRepository.approveIfPending(squadId, targetUserId);
        if (updated == 0) {
            squadRepository.tryDecreaseCount(squadId);
            throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
        }

        squadRepository.closeIfFull(squadId);
    }



    @DistributeLock(keyPrefix = "squad:", argIndex = 2, waitTime = 3, leaseTime = 10)
    @Transactional
    public void rejectParticipant(User owner, Long targetUserId, Long squadId) {

        Squad squad = squadFinder.findSquad(squadId);
        squadValidator.validateOwner(owner, squad);

        if (squad.getJoinType() == JoinType.DIRECT) {
            throw new SquadException(ErrorCode.NOT_APPROVAL_SQUAD);
        }

        User target = squadFinder.findUser(targetUserId);

        squadMemberRepository.findBySquadAndUser(squad, target)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));

        int updated = squadMemberRepository.rejectIfPending(squadId, targetUserId);
        if (updated == 0) {
            throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
        }
    }

    @DistributeLock(keyPrefix = "squad:", argIndex = 2, waitTime = 3, leaseTime = 10)
    @Transactional
    public void kickOffParticipant(User owner, Long targetUserId, Long squadId) {

        Squad squad = squadFinder.findSquad(squadId);
        squadValidator.validateOwner(owner, squad);

        User target = squadFinder.findUser(targetUserId);

        SquadMember member = squadMemberRepository.findBySquadAndUser(squad, target)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));

        if (member.isOwner()) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }

        int updated = squadMemberRepository.kickIfJoined(squadId, targetUserId);
        if (updated == 0) {
            throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
        }

        int decreased = squadRepository.tryDecreaseCount(squadId);
        if (decreased == 0) {
            throw new SquadException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        squadRepository.openIfNotFull(squadId);
    }

    @DistributeLock(keyPrefix = "squad:", argIndex = 1, waitTime = 3, leaseTime = 10)
    @Transactional
    public void leaveSquad(User user, Long squadId) {
        Squad squad = squadFinder.findSquad(squadId);

        SquadMember member = squadMemberRepository.findBySquadAndUser(squad, user)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));

        if (member.isOwner()) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }

        int changed = squadMemberRepository.leaveIfJoined(squadId, user.getId());
        if (changed > 0) {
            int decreased = squadRepository.tryDecreaseCount(squadId);
            if (decreased == 0) {
                throw new SquadException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            squadRepository.openIfNotFull(squadId);
            return;
        }

        int deletedPending = squadMemberRepository.deleteIfPending(squadId, user.getId());
        if (deletedPending > 0) {
            return;
        }

        throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
    }
}
