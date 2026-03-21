package FeedStudy.StudyFeed.policy.domain;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PolicySettings extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String privacy;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Builder
    public PolicySettings(String privacy, String terms) {
        this.privacy = privacy;
        this.terms = terms;
    }

    public void update(String privacy, String terms) {
        if (privacy != null) {
            this.privacy = privacy;
        }
        if (terms != null) {
            this.terms = terms;
        }
    }
}