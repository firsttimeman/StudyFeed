package FeedStudy.StudyFeed.entity;

import java.io.Serializable;
import java.util.Objects;

public class RegionId implements Serializable {
    private String mainRegion;
    private String subRegion;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RegionId regionId = (RegionId) obj;
        return mainRegion.equals(regionId.mainRegion) && subRegion.equals(regionId.subRegion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainRegion, subRegion);
    }
}
