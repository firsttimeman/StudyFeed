package FeedStudy.StudyFeed.notice.repository;

import FeedStudy.StudyFeed.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {


    @Query("""
    select n
    from Notice n
    where n.visible = true
      and (n.publishDate is null or n.publishDate <= :now)
    order by n.createdAt desc
    """)
    List<Notice> findVisibleNotices(@Param("now") LocalDate now);

    List<Notice> findAllByOrderByCreatedAtDesc();

    List<Notice> findAllByVisibleFalseOrderByCreatedAtDesc();

}
