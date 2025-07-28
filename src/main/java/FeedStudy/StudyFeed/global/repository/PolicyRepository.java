package FeedStudy.StudyFeed.global.repository;

import FeedStudy.StudyFeed.global.entity.PolicySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<PolicySettings, Long> {

}
