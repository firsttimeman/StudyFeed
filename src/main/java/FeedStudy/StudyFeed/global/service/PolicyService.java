package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.global.entity.PolicySettings;
import FeedStudy.StudyFeed.global.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicySettings createPolicy(String privacy, String terms) {
        if (!policyRepository.findAll().isEmpty()) {
            throw new RuntimeException("정책 정보는 이미 존재합니다.");
        }

        PolicySettings policy = PolicySettings.builder()
                .privacy(privacy)
                .terms(terms)
                .build();

        return policyRepository.save(policy);
    }


    public PolicySettings getPolicy() {
        return policyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("정책 정보가 존재하지 않습니다."));
    }


    public PolicySettings updatePolicy(String privacy, String terms) {
        PolicySettings policy = getPolicy();
        if (privacy != null) {
            policy.setPrivacy(privacy);
        }
        if (terms != null) {
            policy.setTerms(terms);
        }
        return policyRepository.save(policy);
    }

    public void deletePolicy() {
        PolicySettings policy = getPolicy();
        policyRepository.delete(policy);
    }


}
