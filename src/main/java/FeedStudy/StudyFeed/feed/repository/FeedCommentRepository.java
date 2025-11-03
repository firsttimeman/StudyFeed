package FeedStudy.StudyFeed.feed.repository;


import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {

    // 연관이 꼭 필요한 케이스만 전용 메서드로
    @EntityGraph(attributePaths = {"feed", "parentComment", "user"})
    @Query("select c from FeedComment c where c.id = :id")
    Optional<FeedComment> findByIdWithFeedAndParentAndUser(@Param("id") Long id);

    List<FeedComment> findByUser(User user);


    // 루트 댓글: 최신순(desc)
    @EntityGraph(attributePaths = {"user"})   //
    @Query("""
  select c
  from FeedComment c
  where c.feed.id = :feedId and c.parentComment is null
  order by c.id desc
""")
    Page<FeedComment> findRootComments(@Param("feedId") Long feedId, Pageable pageable);


    @EntityGraph(attributePaths = {"user"})
    List<FeedComment> findTopByParentCommentIdOrderByIdAsc(Long parentCommentId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
    select c
    from FeedComment c
    where c.parentComment.id = :parentId
    order by c.id asc
""")
    Page<FeedComment> findReplies(@Param("parentId") Long parentId, Pageable pageable);

    @Modifying
    @Query("update FeedComment c set c.replyCount = c.replyCount + 1 where c.id = :parentId")
    int increaseReplyCount(@Param("parentId") Long parentId);

    @Modifying
    @Query("update FeedComment c set c.replyCount = c.replyCount - 1 " +
           "where c.id = :parentId and c.replyCount > 0")
    int decreaseReplyCount(@Param("parentId") Long parentId);

    @Modifying
    @Query("""
    update FeedComment c
       set c.deleted = true,
           c.content = '작성자에 의해 삭제된 댓글 입니다.',
           c.user = null
     where c.user.id = :uid
       and c.feed.user.id <> :uid
""")
    int softDeleteOthersByUser(Long uid);


}
