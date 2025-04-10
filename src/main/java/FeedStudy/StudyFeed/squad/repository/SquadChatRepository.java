package FeedStudy.StudyFeed.squad.repository;

import FeedStudy.StudyFeed.squad.entity.SquadChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SquadChatRepository extends JpaRepository<SquadChatMessage, Long> {

    List<SquadChatMessage> findBySquadIdOrderByCreatedAtDesc(Long squadId);
}
