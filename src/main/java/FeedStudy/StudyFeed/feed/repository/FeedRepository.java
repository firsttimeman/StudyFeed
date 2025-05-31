package FeedStudy.StudyFeed.feed.repository;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<Feed, Long> {
    Optional<Feed> findById(Long id);

    Page<Feed> findByUser(User user, Pageable pageable);

    Page<Feed> findByUserNotIn(List<User> excludedUsers, Pageable pageable);

    @Query("select distinct f from Feed f left join fetch f.comments where f.id = :feedId")
    Optional<Feed> findByIdWithComments(@Param("feedId") Long feedId);
}
