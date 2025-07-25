package FeedStudy.StudyFeed.openchat.repository;

import FeedStudy.StudyFeed.global.type.ChatType;
import FeedStudy.StudyFeed.openchat.entity.ChatMessage;
import FeedStudy.StudyFeed.openchat.entity.ChatRoom;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    void deleteByChatRoomIdAndNoticeIsNotNull(Long roomId);


    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId ORDER BY m.id DESC")
    List<ChatMessage> findLatestMessages(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.id < :lastMessageId ORDER BY m.id DESC")
    List<ChatMessage> findPreviousMessages(@Param("roomId") Long roomId, @Param("lastMessageId") Long lastMessageId, Pageable pageable);


    @Query(value = """
        SELECT COUNT(c)
        FROM ChatMessage c
        WHERE c.chatRoom.id = :roomId
        AND c.type = 'DATE'
        AND c.createdAt BETWEEN :start AND :end
        """)
    long countByTodayDateChat(@Param("roomId") Long roomId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<ChatMessage> findByChatRoomAndSender(ChatRoom room, User user);
}
