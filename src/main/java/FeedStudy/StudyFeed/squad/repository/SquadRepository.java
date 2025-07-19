package FeedStudy.StudyFeed.squad.repository;

import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SquadRepository extends JpaRepository<Squad, Long> {

    Page<Squad> findByUser(User user, Pageable pageable);
    List<Squad> findByUser(User user);

    @Query("""
                SELECT s FROM Squad s
                WHERE (:category = '전체' OR s.category = :category)
                AND (:regionMain = '전체' OR s.regionMain = :regionMain)
                AND (:regionSub = '전체' OR s.regionSub = :regionSub)
                AND (:recruitingOnly = false OR s.closed = false)
                AND s.date >= :sevenDaysAgo
            """)
    Page<Squad> findFilteredSquads(@Param("category") String category,
                                   @Param("regionMain") String regionMain,
                                   @Param("regionSub") String regionSub,
                                   @Param("recruitingOnly") boolean recruitingOnly,
                                   @Param("sevenDaysAgo") LocalDate sevenDaysAgo,
                                   Pageable pageable);

    @Query("""
            SELECT s FROM Squad s
            WHERE (:category = '전체' OR s.category = :category)
            AND (:regionMain = '전체' OR s.regionMain = :regionMain)
            AND (:regionMain = '전체' OR :regionSub = '전체' OR s.regionSub = :regionSub)
            AND (:recruitingOnly = false OR s.closed = false)
            AND s.date >= :sevenDaysAgo
            AND s.user NOT IN :excludedUsers
            """)
    Page<Squad> findFilteredSquadsWithExclusion(@Param("category") String category,
                                                @Param("regionMain") String regionMain,
                                                @Param("regionSub") String regionSub,
                                                @Param("recruitingOnly") boolean recruitingOnly,
                                                @Param("excludedUsers") List<User> excludedUsers,
                                                @Param("sevenDaysAgo") LocalDate sevenDaysAgo,
                                                Pageable pageable);


    @Query("""
            SELECT s FROM Squad s
            LEFT JOIN FETCH s.members m
            LEFT JOIN FETCH m.user
            WHERE s.id = :squadId
            """)
    Optional<Squad> findByIdWithParticipants(@Param("squadId") Long squadId);
}
