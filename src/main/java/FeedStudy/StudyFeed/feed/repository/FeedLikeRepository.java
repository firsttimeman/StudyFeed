package FeedStudy.StudyFeed.feed.repository;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedLike;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    boolean existsByUserAndFeed(User user, Feed feed);

    long deleteByFeedIdAndUserId(Long feedId, Long userId);

    @Query("""
    select fl.feed.id
    from FeedLike fl
    where fl.user = :user
        and fl.feed.id in :feedIds
""")
    List<Long> findLikedFeedIds(@Param("user") User user, @Param("feedIds") Collection<Long> feedIds);

    @Modifying
    @Query("delete from FeedLike l where l.user.id = :uid")
    int deleteAllByUserId(Long uid);


    @Modifying
    @Query("""
    update Feed f
    set f.likeCount = f.likeCount + 1
    where f.id = :feedId
""")
    void incrementLikeCount(@Param("feedId") Long feedId);


    @Modifying
    @Query("update Feed f set f.likeCount = f.likeCount - 1 where f.id = :feedId and f.likeCount > 0")
    void decreaseLikeCount(@Param("feedId") Long feedId);
}
