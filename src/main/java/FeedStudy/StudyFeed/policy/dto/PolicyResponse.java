package FeedStudy.StudyFeed.policy.dto;

import FeedStudy.StudyFeed.policy.domain.PolicySettings;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PolicyResponse {

    private String privacy;
    private String terms;

    public static PolicyResponse from(PolicySettings policy) {
        return new PolicyResponse(
                policy.getPrivacy(),
                policy.getTerms()
        );
    }
}