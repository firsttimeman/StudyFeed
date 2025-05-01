package FeedStudy.StudyFeed.feed.repository;


import FeedStudy.StudyFeed.feed.entity.FeedComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {
    Page<FeedComment> findByParentId(Long parentId, Pageable pageable);

    List<FeedComment> findByParentId(Long parentId, Sort sort);


}
