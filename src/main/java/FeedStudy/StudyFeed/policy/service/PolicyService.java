package FeedStudy.StudyFeed.policy.service;

import FeedStudy.StudyFeed.policy.domain.PolicySettings;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.PolicyException;
import FeedStudy.StudyFeed.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicySettings createPolicy(String privacy, String terms) {
        if (policyRepository.findTopByOrderByIdAsc().isPresent()) {
            throw new PolicyException(ErrorCode.POLICY_ALREADY_EXISTS);
        }

        PolicySettings policy = PolicySettings.builder()
                .privacy(privacy)
                .terms(terms)
                .build();

        return policyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public PolicySettings getPolicy() {
        return policyRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new PolicyException(ErrorCode.POLICY_NOT_FOUND));
    }

    public PolicySettings updatePolicy(String privacy, String terms) {
        PolicySettings policy = getPolicy();
        policy.update(privacy, terms);
        return policy;
    }

    public void deletePolicy() {
        PolicySettings policy = getPolicy();
        policyRepository.delete(policy);
    }
}