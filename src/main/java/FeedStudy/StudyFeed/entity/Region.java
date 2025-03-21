package FeedStudy.StudyFeed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Setter
@DynamicInsert
@NoArgsConstructor
@IdClass(RegionId.class)
public class Region {
    @Id
    @Column
    private String mainRegion;

    @Id
    @Column
    private String subRegion;

    private int position;

    public Region(String mainRegion, String subRegion) {
        this.mainRegion = mainRegion;
        this.subRegion = subRegion;
    }
}
