package FeedStudy.StudyFeed.openchat.repository;

import FeedStudy.StudyFeed.openchat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
