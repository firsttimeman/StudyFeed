package FeedStudy.StudyFeed.repository;

import FeedStudy.StudyFeed.entity.Block;
import FeedStudy.StudyFeed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);
    List<Block> findByBlocker(User blocker);

    List<Block> findByBlocked(User blocked);
}
