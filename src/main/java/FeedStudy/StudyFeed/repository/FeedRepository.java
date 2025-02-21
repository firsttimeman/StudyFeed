package FeedStudy.StudyFeed.repository;

import FeedStudy.StudyFeed.entity.Feed;
import FeedStudy.StudyFeed.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedRepository extends JpaRepository<Feed, Long> {
    Optional<Feed> findById(Long id);

    Page<Feed> findByUser(User user, Pageable pageable);
}
