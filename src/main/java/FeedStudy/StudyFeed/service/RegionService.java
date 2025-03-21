package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.dto.RegionRequest;
import FeedStudy.StudyFeed.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.entity.Region;
import FeedStudy.StudyFeed.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    public void checkRegion(RegionRequest requestDto) {

        //TODO interface로 update + Create dto 처리하기 이게 머였지?


        if( !regionRepository.existsByMainRegion(requestDto.getRegionMain())) {
            throw new IllegalArgumentException("도·시를 확인해주세요~");
        }

        if(requestDto.getRegionSub() != null && regionRepository
                .existsByMainRegionAndSubRegion(requestDto.getRegionMain(),
                requestDto.getRegionSub())) {
            throw new IllegalArgumentException("도·시 또는 시·구·군을 확인해주세요");
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
