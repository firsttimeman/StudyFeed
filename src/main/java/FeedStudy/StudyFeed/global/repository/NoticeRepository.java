package FeedStudy.StudyFeed.global.repository;

import FeedStudy.StudyFeed.global.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {


    @Query("SELECT n FROM Notice n WHERE n.visible = true AND n.publishDate <= :now ORDER BY n.publishDate DESC")
    List<Notice> findVisibleNotices(@Param("now")LocalDate now);

    List<Notice> findAllByOrderByCreatedAtDesc();

    List<Notice> findAllByIsVisibleFalseOrderByCreatedAtDesc();

}
