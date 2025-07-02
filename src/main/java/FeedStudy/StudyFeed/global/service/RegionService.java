package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.global.dto.RegionRequest;
import FeedStudy.StudyFeed.global.entity.Region;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.RegionException;
import FeedStudy.StudyFeed.global.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    public void checkRegion(String regionMain, String regionSub) {

        if(!regionRepository.existsByMainRegion(regionMain)) {
           throw new RegionException(ErrorCode.REGION_MAIN_NOT_FOUND);
        }

        if(regionSub != null && !regionRepository.existsByMainRegionAndSubRegion(regionMain, regionSub)) {
            throw new RegionException(ErrorCode.REGION_SUB_NOT_FOUND);
        }

    }


    // todo Region main 에 대한 fetch 기능 전체 도시 가지고 오기
    public List<String> getAllCities() {
        return regionRepository.findDistinctMainRegion();
    }


    //todo region sub는 main 에 따른 전체 시구군 가지고 오기
    public List<Region> getSubRegionsByCity(String mainRegion) {
        return regionRepository.findAllByMainRegionOrderByPositionAsc(mainRegion);
    }
}
