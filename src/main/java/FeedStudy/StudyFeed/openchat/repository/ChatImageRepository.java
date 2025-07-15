package FeedStudy.StudyFeed.openchat.repository;

import FeedStudy.StudyFeed.openchat.entity.ChatImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatImageRepository extends JpaRepository<ChatImage, Long> {

}
