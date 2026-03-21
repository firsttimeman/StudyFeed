package FeedStudy.StudyFeed.policy.repository;

import FeedStudy.StudyFeed.policy.domain.PolicySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<PolicySettings, Long> {

    Optional<PolicySettings> findTopByOrderByIdAsc();
}