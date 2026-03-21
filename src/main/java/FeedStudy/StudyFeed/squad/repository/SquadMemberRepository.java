// src/main/java/FeedStudy/StudyFeed/squad/repository/SquadMemberRepository.java
package FeedStudy.StudyFeed.squad.repository;

import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.squad.dto.PendingApplicantDto;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SquadMemberRepository extends JpaRepository<SquadMember, Long> {

    Optional<SquadMember> findBySquadAndUser(Squad squad, User user);
    List<SquadMember> findAllBySquad(Squad squad);
    List<SquadMember> findByUser(User user);

    // 이미 참여/신청 여부(빠른 존재 확인)
    boolean existsBySquadIdAndUserId(Long squadId, Long userId);

    // JOINED 인원 수 (정원 체크용)
    long countBySquadIdAndMembershipStatus(Long squadId, MembershipStatus status);


    @Modifying
    @Query("""
update SquadMember m
   set m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.JOINED
 where m.squad.id = :squadId
   and m.user.id = :userId
   and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.LEFT
""")
    int rejoinIfLeft(@Param("squadId") Long squadId,
                     @Param("userId") Long userId);

    @Modifying
    @Query("""
update SquadMember m
   set m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.PENDING
 where m.squad.id = :squadId
   and m.user.id = :userId
   and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.LEFT
""")
    int requestAgainIfLeft(@Param("squadId") Long squadId,
                           @Param("userId") Long userId);

    @Query("""
    select new FeedStudy.StudyFeed.user.dto.UserSimpleDto(
        u.nickName,
        u.imageUrl,
        u.gender,
        u.birthDate
    )
    from SquadMember m
    join m.user u
    where m.squad.id = :squadId
      and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.JOINED
    """)
    List<UserSimpleDto> findJoinedParticipants(@Param("squadId") Long squadId);

    boolean existsBySquadIdAndMembershipStatus(Long squadId, MembershipStatus membershipStatus);

    @Query("""
    select m.membershipStatus
    from SquadMember m
    where m.squad.id = :squadId
      and m.user.id  = :userId
    """)
    Optional<MembershipStatus> findMyMembership(@Param("squadId") Long squadId,
                                                @Param("userId") Long userId);


    @Query("""
select new FeedStudy.StudyFeed.squad.dto.PendingApplicantDto(
    u.nickName, u.imageUrl, u.gender, u.birthDate, u.email
)
from SquadMember m
join m.user u
where m.squad.id = :squadId
  and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.PENDING
order by m.id asc
""")
    List<PendingApplicantDto> findPendingApplicants(@Param("squadId") Long squadId);



    @Query("""
    select (count(m) > 0)
    from SquadMember m
    where m.squad.id = :squadId
      and m.membershipStatus = :status
      and m.user.id <> :ownerId
      and (m.user.birthDate < :minBirth or m.user.birthDate > :maxBirth)
""")
    boolean existsJoinedOutOfAge(@Param("squadId") Long squadId,
                                 @Param("ownerId") Long ownerId,
                                 @Param("status") MembershipStatus status,
                                 @Param("minBirth") LocalDate minBirth,
                                 @Param("maxBirth") LocalDate maxBirth);


    @Query("""
select (count(m) > 0)
from SquadMember m
where m.squad.id = :squadId
  and m.membershipStatus = :status
  and m.user.gender <> :requiredGender
""")
    boolean existsJoinedGenderConflict(@Param("squadId") Long squadId,
                                       @Param("status") MembershipStatus status,
                                       @Param("requiredGender") Gender requiredGender);


    @Modifying
    @Query("delete from SquadMember m where m.squad.id = :squadId")
    void deleteBySquadId(@Param("squadId") Long squadId);


    @Query("""
  select count(m) from SquadMember m
  where m.squad.id = :squadId
    and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.PENDING
""")
    long countPendingBySquadId(@Param("squadId") Long squadId);


    @Modifying
    @Query("""
update SquadMember m
   set m.membershipStatus = 'JOINED'
 where m.squad.id = :squadId
   and m.user.id  = :userId
   and m.membershipStatus = 'PENDING'
""")
    int approveIfPending(@Param("squadId") Long squadId, @Param("userId") Long userId);


    @Modifying
    @Query("""
update SquadMember m
   set m.membershipStatus = 'REJECTED'
 where m.squad.id = :squadId
   and m.user.id  = :userId
   and m.membershipStatus = 'PENDING'
""")
    int rejectIfPending(@Param("squadId") Long squadId, @Param("userId") Long userId);


    @Modifying
    @Query("""
update SquadMember m
   set m.membershipStatus = 'KICKED_OUT'
 where m.squad.id = :squadId
   and m.user.id  = :userId
   and m.membershipStatus = 'JOINED'
""")
    int kickIfJoined(@Param("squadId") Long squadId, @Param("userId") Long userId);

    @Modifying
    @Query("""
update SquadMember m
   set m.membershipStatus = 'LEFT'
 where m.squad.id = :squadId
   and m.user.id  = :userId
   and m.membershipStatus = 'JOINED'
""")
    int leaveIfJoined(@Param("squadId") Long squadId, @Param("userId") Long userId);


    @Modifying
    @Query("""
delete from SquadMember m
 where m.squad.id = :squadId
   and m.user.id  = :userId
   and m.membershipStatus = 'PENDING'
""")
    int deleteIfPending(@Param("squadId") Long squadId, @Param("userId") Long userId);

    boolean existsBySquadIdAndUserIdAndMembershipStatus(Long squadId, Long userId,  MembershipStatus status);


    @Query("""
    select m.squad.id from SquadMember m
     where m.user.id = :uid
       and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.JOINED
""")
    List<Long> findJoinedSquadIds(Long uid);

    @Modifying
    @Query("delete from SquadMember m where m.user.id = :uid")
    int deleteAllJoined(Long uid);

    @Modifying
    @Query("delete from SquadMember m" +
           " where m.user.id = :uid " +
           "and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.PENDING")
    int deleteAllPending(Long uid);

    @Modifying
    @Query("delete from SquadMember m " +
           "where m.user.id = :uid " +
           "and m.membershipStatus <> FeedStudy.StudyFeed.global.type.MembershipStatus.JOINED")
    int cleanupNonJoined(Long uid);
}