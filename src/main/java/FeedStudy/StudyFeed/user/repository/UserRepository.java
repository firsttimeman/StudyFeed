package FeedStudy.StudyFeed.user.repository;

import FeedStudy.StudyFeed.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Boolean existsByFcmToken(String fcmToken);
    Boolean existsByNickName(String nickName);

    // select * from user where id!=:id and nick_name=:nickName
    Boolean existsByIdNotAndNickName(Long id, String nickName);

    @Modifying
    @Query("update User u set u.reportCount = u.reportCount + 1 where u.id = :id")
    int increaseReportCount(@Param("id") Long id);


}
