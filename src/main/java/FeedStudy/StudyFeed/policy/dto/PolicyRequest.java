package FeedStudy.StudyFeed.policy.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyRequest {

    @Size(max = 10000, message = "개인정보처리방침은 10000자 이하여야 합니다.")
    private String privacy;

    @Size(max = 10000, message = "이용약관은 10000자 이하여야 합니다.")
    private String terms;
}