package FeedStudy.StudyFeed.block.repository;

import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);



    List<Block> findByBlocker(User blocker);

    List<Block> findByBlocked(User blocked);



    @Query("SELECT b FROM Block b JOIN FETCH b.blocked where b.blocker = :blocker")
    List<Block> findByBlockerWithBlocked(@Param("blocker") User blocker);

    @Query("select b from Block b join fetch b.blocker where b.blocked = :blocked")
    List<Block> findByBlockedWithBlocker(@Param("blocked") User blocked);
}
