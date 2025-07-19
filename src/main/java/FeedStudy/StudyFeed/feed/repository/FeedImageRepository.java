package FeedStudy.StudyFeed.feed.repository;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedImageRepository  extends JpaRepository<FeedImage, Long> {
    void deleteByImageUrl(String imageUrl);

    List<FeedImage> findByFeed(Feed feed);

    void deleteAllByFeed(Feed feed);
}
