package FeedStudy.StudyFeed.feed.repository;

import FeedStudy.StudyFeed.feed.dto.ImageRow;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FeedImageRepository  extends JpaRepository<FeedImage, Long> {


    List<FeedImage> findAllByFeedIn(List<Feed> feeds);
    List<FeedImage> findAllByImageUrlIn(Collection<String> urls);


    @Query("""
    SELECT fi
    FROM FeedImage fi
    JOIN FETCH fi.feed
    WHERE fi.imageUrl IN :urls
""")
    List<FeedImage> findAllByImageUrlInWithFeed(@Param("urls") List<String> urls);


    List<FeedImage> findByFeedIdOrderByIdAsc(Long feedId);

    @Query("""
      select new FeedStudy.StudyFeed.feed.dto.ImageRow(fi.feed.id, fi.imageUrl)
      from FeedImage fi
      where fi.feed.id in :feedIds
      order by fi.feed.id asc, fi.id asc
    """)
    List<ImageRow> findPairsByFeedIdIn(@Param("feedIds") Collection<Long> feedIds);

    @Query("select fi.imageUrl from FeedImage fi where fi.feed = :feed")
    List<String> findImageUrlsByFeed(@Param("feed") Feed feed);


    @Query("select i.imageUrl from FeedImage i where i.feed.id in :feedIds")
    List<String> findUrlsByFeedIds(List<Long> feedIds);
}
