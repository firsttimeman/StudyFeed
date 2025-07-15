package FeedStudy.StudyFeed.openchat.repository;

import FeedStudy.StudyFeed.openchat.entity.ChatRoom;
import FeedStudy.StudyFeed.openchat.entity.ChatRoomUser;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    boolean existsByChatRoomAndUser(ChatRoom room, User user);

    Optional<ChatRoomUser> findByChatRoomAndUser(ChatRoom room, User user);
}
