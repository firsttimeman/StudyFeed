package FeedStudy.StudyFeed.repository;


import FeedStudy.StudyFeed.entity.Feed.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {


}
