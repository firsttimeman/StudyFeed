package FeedStudy.StudyFeed.squad.repository;

import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SquadRepository extends JpaRepository<Squad, Long> {


    List<Squad> findByUser(User user);

    @Query("""
select s from Squad s
where (:category is null or s.category = :category)
and (:regionMain = '전체' or s.regionMain = :regionMain)
and (:regionSub = '전체' or s.regionSub = :regionSub)
and (:recruitingOnly = false or (s.closed = false and s.currentCount < s.maxParticipants))
order by s.date, s.time, s.createdAt desc

""")
    Page<Squad> findFilteredSquads(@Param("category") Topic category,
                                   @Param("regionMain") String regionMain,
                                   @Param("regionSub") String regionSub,
                                   @Param("recruitingOnly") boolean recruitingOnly,
                                   Pageable pageable);





    @Query("""
            SELECT s FROM Squad s
            LEFT JOIN FETCH s.members m
            LEFT JOIN FETCH m.user
            WHERE s.id = :squadId
            """)
    Optional<Squad> findByIdWithParticipants(@Param("squadId") Long squadId);


    @Query("""
select s
from Squad s
where s.user.id = :userId
or exists (
select 1 from SquadMember m
where m.squad = s
and m.user.id = :userId
and m.membershipStatus = FeedStudy.StudyFeed.global.type.MembershipStatus.JOINED
)
""")
    Page<Squad> findAllMine(@Param("userId") Long userId, Pageable pageable);


    @Query("""
select distinct s
from Squad s
left join fetch s.members m
left join fetch m.user
where s.id = :squadId
""")
    Optional<Squad> findWithMembersAndUsers(@Param("squadId") Long squadId);

    @Modifying
    @Query("""
        update Squad s
           set s.currentCount = s.currentCount + 1
         where s.id = :squadId
           and s.currentCount < s.maxParticipants
    """)
    int tryIncreaseCount(@Param("squadId") Long squadId);


    @Modifying
    @Query("""
        update Squad s
           set s.closed = true
         where s.id = :squadId
           and s.closed = false
           and s.currentCount >= s.maxParticipants
    """)
    int closeIfFull(@Param("squadId") Long squadId);



    @Modifying
    @Query("""
update Squad s
   set s.maxParticipants = :newMax
 where s.id = :squadId
   and :newMax >= s.currentCount
""")
    int tryUpdateMaxParticipants(@Param("squadId") Long squadId, @Param("newMax") int newMax);


    @Modifying
    @Query("""
update Squad s
   set s.closed = false
 where s.id = :squadId
   and s.closed = true
   and s.currentCount < s.maxParticipants
""")
    int openIfNotFull(@Param("squadId") Long squadId);


    @Modifying
    @Query("""
update Squad s
   set s.currentCount = s.currentCount - 1
 where s.id = :squadId
   and s.currentCount > 0
""")
    int tryDecreaseCount(@Param("squadId") Long squadId);

    @Modifying
    @Query("update Squad s set s.reportCount = s.reportCount + 1 where s.id = :id")
    int increaseReportCount(@Param("id") Long id);

    @Query("select s.id from Squad s where s.user.id = :uid")
    List<Long> findIdsByOwner(Long uid);

    @Modifying
    @Query("delete from Squad s where s.user.id = :uid")
    int deleteAllByOwner(Long uid);

}
