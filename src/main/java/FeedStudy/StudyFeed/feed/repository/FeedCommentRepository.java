package FeedStudy.StudyFeed.feed.repository;


import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {
    Page<FeedComment> findByParentComment_Id(Long parentId, Pageable pageable);


    Optional<FeedComment> findByParentComment_IdAndId(Long parentId, Long userId);

    Optional<FeedComment> findByIdAndUser_Id(Long id, Long userId);

    List<FeedComment> findByUser(User user);
}
