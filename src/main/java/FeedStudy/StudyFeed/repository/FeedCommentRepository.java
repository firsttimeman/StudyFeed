package FeedStudy.StudyFeed.repository;


import FeedStudy.StudyFeed.entity.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {


}
