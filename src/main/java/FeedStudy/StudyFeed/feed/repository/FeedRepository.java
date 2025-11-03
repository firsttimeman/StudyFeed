package FeedStudy.StudyFeed.feed.repository;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<Feed, Long> {


    @EntityGraph(attributePaths = {"user"})
    @Query("""
    select f
    from Feed f
    where (:category is null or f.category = :category)
        and (:excludedEmpty = true or f.user.id not in :excludedUserIds)
    order by f.id desc
""")
    Page<Feed> findHomeFeeds(@Param("category") Topic category,
                             @Param("excludedEmpty") boolean excludedEmpty,
                             @Param("excludedUserIds") Collection<Long> excludedUserIds,
                             Pageable pageable);


    @EntityGraph(attributePaths = {"user"})
    @Query("select f from Feed f where f.id = :id")
    Optional<Feed> findDetailForView(@Param("id") Long id);

    @Modifying
    @Query("update Feed f set f.commentCount = f.commentCount + 1 where f.id = :feedId")
    int increaseCommentCount(@Param("feedId") Long feedId);

    @Modifying
    @Query("update Feed f set f.commentCount = f.commentCount - 1 " +
           "where f.id = :feedId and f.commentCount > 0")
    int decreaseCommentCount(@Param("feedId") Long feedId);



    @EntityGraph(attributePaths = {"user"})
    @Query("""
    select f
    from Feed f
    where f.user.id = :userId
    order by f.id desc 
""")
    Page<Feed> findMyFeeds(@Param("userId") Long userId, Pageable pageable);



    @Modifying
    @Query("update Feed f set f.reportCount = f.reportCount + 1 where f.id = :id")
    int increaseReportCount(@Param("id") Long id);

    @Query("select f.id from Feed f where f.user.id = :uid")
    List<Long> findIdsByOwner(Long uid);

    @Modifying
    @Query("delete from Feed f where f.user.id = :uid")
    int deleteAllByOwner(Long uid);

}
