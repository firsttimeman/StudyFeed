package FeedStudy.StudyFeed.openchat.repository;

import FeedStudy.StudyFeed.openchat.entity.ChatRoom;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
