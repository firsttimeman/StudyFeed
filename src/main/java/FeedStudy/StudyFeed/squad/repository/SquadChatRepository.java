//package FeedStudy.StudyFeed.squad.repository;
//
//import FeedStudy.StudyFeed.squad.entity.SquadChat;
//import io.lettuce.core.dynamic.annotation.Param;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.time.LocalDateTime;
//
//public interface SquadChatRepository extends JpaRepository<SquadChat, Long> {
//
//
//    @Query("""
//            SELECT COUNT(c) > 0
//            FROM SquadChat c
//            WHERE c.squad.id = :squadId
//            AND c.type = 'DATE'
//            AND c.createdAt BETWEEN :start AND :end
//            """)
//    boolean existsByTodayDateChat(@Param("squadId") Long squadId,
//                                  @Param("start") LocalDateTime start,
//                                  @Param("end") LocalDateTime end);
//}
