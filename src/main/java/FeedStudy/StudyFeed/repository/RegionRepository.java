package FeedStudy.StudyFeed.repository;

import FeedStudy.StudyFeed.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Boolean existsByMainRegionAndSubRegion(String main, String sub);

    List<Region> findAllByMainRegionOrderByPositionAsc(String main);

    Boolean existsByMainRegion(String main);

    @Query("select distinct r.mainRegion from Region r order by r.mainRegion ASC")
    List<String> findDistinctMainRegion();
}
