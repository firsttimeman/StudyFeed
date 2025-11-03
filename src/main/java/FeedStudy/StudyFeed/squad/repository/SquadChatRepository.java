package FeedStudy.StudyFeed.squad.repository;

import FeedStudy.StudyFeed.global.type.ChatType;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadChat;
import FeedStudy.StudyFeed.user.entity.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SquadChatRepository extends JpaRepository<SquadChat, Long> {


    @Query("""
        select count(c)
        from SquadChat c
        where c.squad.id = :squadId
          and c.type = FeedStudy.StudyFeed.global.type.ChatType.DATE
          and c.createdAt >= :start
          and c.createdAt <  :end
    """)
    long countByTodayDateChat(@Param("squadId") Long squadId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);




    // 최근/이전 메시지 조회 (이미 쓰고 계신 것들 예시)
    @Query("select c from SquadChat c where c.squad.id = :squadId order by c.id desc")
    List<SquadChat> findLatestChats(@Param("squadId") Long squadId, Pageable pageable);

    @Query("""
        select c from SquadChat c
        where c.squad.id = :squadId and c.id < :lastId
        order by c.id desc
    """)
    List<SquadChat> findPreviousChats(@Param("squadId") Long squadId,
                                      @Param("lastId") Long lastId,
                                      Pageable pageable);

    @Modifying
    @Query("""
    delete from SquadChat c
    where c.squad.id = :squadId
      and c.type = FeedStudy.StudyFeed.global.type.ChatType.NOTICE
""")
    void deleteBySquadIdAndNoticeIsNotNull(@Param("squadId") Long squadId);


    @Modifying
    @Query("""
    update SquadChat c
       set c.user = null,
           c.message = '삭제된 메세지 입니다.',
           c.deletable = false,
           c.type = FeedStudy.StudyFeed.global.type.ChatType.TEXT
     where c.user.id = :uid
""")
    int softDeleteAllByAuthor(Long uid);

    @Query("select i.uniqueName from SquadChatImage i where i.squadChat.user.id = :uid")
    List<String> findAllImageKeysByAuthor(Long uid);

    @Modifying
    @Query("delete from SquadChatImage i where i.squadChat.user.id = :uid")
    int deleteAllImagesByAuthor(Long uid);
}
