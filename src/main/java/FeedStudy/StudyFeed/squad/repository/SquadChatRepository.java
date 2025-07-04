package FeedStudy.StudyFeed.squad.repository;

import FeedStudy.StudyFeed.global.type.ChatType;
import FeedStudy.StudyFeed.squad.entity.SquadChat;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SquadChatRepository extends JpaRepository<SquadChat, Long> {


    @Query(value = """
            SELECT COUNT(c)
            FROM SquadChat c
            WHERE c.squad.id = :squadId
            AND c.type = 'DATE'
            AND c.createdAt BETWEEN :start AND :end
            """, nativeQuery = false)
    long countByTodayDateChat(@Param("squadId") Long squadId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);




    @Query("SELECT c FROM SquadChat c WHERE c.squad.id" +
           " = :squadID ORDER BY c.id DESC ")
    List<SquadChat> findLatestChats(@Param("squadId") Long squadId, Pageable pageable);


    @Query("SELECT c from SquadChat c WHERE c.squad.id = :squadId AND c.id < :lastId ORDER BY c.id DESC")
    List<SquadChat> findPreviousChats(@Param("squadId") Long squadId, @Param("lastId") Long lastId, Pageable pageable);

    void deleteBySquadIdAndType(Long squadId, ChatType type);
}
