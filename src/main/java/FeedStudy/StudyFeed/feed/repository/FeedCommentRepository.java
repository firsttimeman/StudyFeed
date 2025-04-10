package FeedStudy.StudyFeed.feed.repository;


import FeedStudy.StudyFeed.feed.entity.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {


}
