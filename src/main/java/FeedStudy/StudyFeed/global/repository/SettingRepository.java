package FeedStudy.StudyFeed.global.repository;

import FeedStudy.StudyFeed.global.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

}
