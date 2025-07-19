package FeedStudy.StudyFeed.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Setting extends BaseEntity{

    @Column(columnDefinition = "TEXT")
    private String privacy;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Builder.Default
    private boolean isUnderMaintenance = false;


}
