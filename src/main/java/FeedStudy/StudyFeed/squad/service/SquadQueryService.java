package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.squad.dto.SquadDetailDto;
import FeedStudy.StudyFeed.squad.dto.SquadFilterRequest;
import FeedStudy.StudyFeed.squad.dto.SquadSimpleDto;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SquadQueryService {

    private final SquadRepository squadRepository;
    private final SquadMemberRepository squadMemberRepository;


    public DataResponse mySquad(User user, Pageable pageable) {

        Pageable fixedPageable = forceSize20(pageable);

        Page<Squad> page = squadRepository.findAllMine(user.getId(), fixedPageable);


        List<SquadSimpleDto> list = page.getContent().stream()
                .map(squad -> {
                    boolean isOwner = squad.getUser().getId().equals(user.getId());
                    String membership = isOwner ? "OWNER" : "JOINED";


                    Long pendingCount = null;
                    if (isOwner && squad.getJoinType() == JoinType.APPROVAL) {
                        pendingCount = squadMemberRepository.countPendingBySquadId(squad.getId()); // todo N+1 가능성
                    }

                    return SquadSimpleDto.toDto(squad, membership, pendingCount);
                })
                .toList();

        return new DataResponse(list, page.hasNext());

    }

    public DataResponse homeSquad(Pageable pageable, SquadFilterRequest req) {

        Pageable fixedPageable = forceSize20(pageable);

        Page<Squad> page = squadRepository.findFilteredSquads(req.getCategory(), req.getRegionMain(),
                req.getRegionSub(), req.isRecruitingOnly(), fixedPageable);

        List<SquadSimpleDto> list = page.getContent().stream()
                .map(s -> SquadSimpleDto.toDto(s, null, null)).toList();

        return new DataResponse(list, page.hasNext());

    }


    @Transactional
    public SquadDetailDto detail(User user, long squadId) {
        Squad squad = squadRepository.findById(squadId)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));

        boolean isOwner = Objects.equals(squad.getUser().getId(), user.getId());

        List<UserSimpleDto> participants = squadMemberRepository.findJoinedParticipants(squadId);

        boolean hasPending = isOwner
                             && squad.getJoinType() == JoinType.APPROVAL
                             && squadMemberRepository.existsBySquadIdAndMembershipStatus(
                squadId,
                MembershipStatus.PENDING
        );

        String myMembership = isOwner
                ? "OWNER"
                : squadMemberRepository.findMyMembership(squadId, user.getId())
                .map(Enum::name)
                .orElse("NONE");

        return SquadDetailDto.toDto(user, squad, participants, hasPending, myMembership);
    }

    private Pageable forceSize20(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(
                Sort.Order.asc("date"),
                Sort.Order.asc("time"),
                Sort.Order.desc("createdAt")
        );

        return PageRequest.of(pageable.getPageNumber(), 20, sort);
    }
}
