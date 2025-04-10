package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.global.entity.Region;
import FeedStudy.StudyFeed.global.repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class StudyFeedApplication {
	@Autowired
	private RegionRepository regionRepository;

	public static void main(String[] args) {
		SpringApplication.run(StudyFeedApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initialize() {
		setRegionList();
	}

	private void setRegionList() {
		Region[] regionList = { new Region("서울", "종로구"), new Region("서울", "중구"), new Region("서울", "용산구"),
				new Region("서울", "성동구"), new Region("서울", "광진구"), new Region("서울", "동대문구"), new Region("서울", "중랑구"),
				new Region("서울", "성북구"), new Region("서울", "강북구"), new Region("서울", "도봉구"), new Region("서울", "노원구"),
				new Region("서울", "은평구"), new Region("서울", "서대문구"), new Region("서울", "마포구"), new Region("서울", "양천구"),
				new Region("서울", "강서구"), new Region("서울", "구로구"), new Region("서울", "금천구"), new Region("서울", "영등포구"),
				new Region("서울", "동작구"), new Region("서울", "관악구"), new Region("서울", "서초구"), new Region("서울", "강남구"),
				new Region("서울", "송파구"), new Region("서울", "강동구"), new Region("부산광역시", "중구"), new Region("부산광역시", "서구"),
				new Region("부산광역시", "동구"), new Region("부산광역시", "영도구"), new Region("부산광역시", "부산진구"),
				new Region("부산광역시", "동래구"), new Region("부산광역시", "남구"), new Region("부산광역시", "북구"),
				new Region("부산광역시", "해운대구"), new Region("부산광역시", "사하구"), new Region("부산광역시", "금정구"),
				new Region("부산광역시", "강서구"), new Region("부산광역시", "연제구"), new Region("부산광역시", "수영구"),
				new Region("부산광역시", "사상구"), new Region("부산광역시", "기장군"), new Region("대구광역시", "중구"),
				new Region("대구광역시", "동구"), new Region("대구광역시", "서구"), new Region("대구광역시", "남구"),
				new Region("대구광역시", "북구"), new Region("대구광역시", "수성구"), new Region("대구광역시", "달서구"),
				new Region("대구광역시", "달성군"), new Region("인천광역시", "중구"), new Region("인천광역시", "동구"),
				new Region("인천광역시", "남구"), new Region("인천광역시", "연수구"), new Region("인천광역시", "남동구"),
				new Region("인천광역시", "부평구"), new Region("인천광역시", "계양구"), new Region("인천광역시", "서구"),
				new Region("인천광역시", "강화군"), new Region("인천광역시", "옹진군"), new Region("광주광역시", "동구"),
				new Region("광주광역시", "서구"), new Region("광주광역시", "남구"), new Region("광주광역시", "북구"),
				new Region("광주광역시", "광산구"), new Region("대전광역시", "동구"), new Region("대전광역시", "중구"),
				new Region("대전광역시", "서구"), new Region("대전광역시", "유성구"), new Region("대전광역시", "대덕구"),
				new Region("울산광역시", "중구"), new Region("울산광역시", "남구"), new Region("울산광역시", "동구"),
				new Region("울산광역시", "북구"), new Region("울산광역시", "울주군"), new Region("세종특별자치시", ""),
				new Region("경기도", "수원시 장안구"), new Region("경기도", "수원시 권선구"), new Region("경기도", "수원시 팔달구"),
				new Region("경기도", "수원시 영통구"), new Region("경기도", "성남시 수정구"), new Region("경기도", "성남시 중원구"),
				new Region("경기도", "성남시 분당구"), new Region("경기도", "의정부시"), new Region("경기도", "안양시 만안구"),
				new Region("경기도", "안양시 동안구"), new Region("경기도", "부천시"), new Region("경기도", "광명시"),
				new Region("경기도", "평택시"), new Region("경기도", "동두천시"), new Region("경기도", "안산시 상록구"),
				new Region("경기도", "안산시 단원구"), new Region("경기도", "고양시 덕양구"), new Region("경기도", "고양시 일산동구"),
				new Region("경기도", "고양시 일산서구"), new Region("경기도", "과천시"), new Region("경기도", "구리시"),
				new Region("경기도", "남양주시"), new Region("경기도", "오산시"), new Region("경기도", "시흥시"), new Region("경기도", "군포시"),
				new Region("경기도", "의왕시"), new Region("경기도", "하남시"), new Region("경기도", "용인시 처인구"),
				new Region("경기도", "용인시 기흥구"), new Region("경기도", "용인시 수지구"), new Region("경기도", "파주시"),
				new Region("경기도", "이천시"), new Region("경기도", "안성시"), new Region("경기도", "김포시"), new Region("경기도", "화성시"),
				new Region("경기도", "광주시"), new Region("경기도", "양주시"), new Region("경기도", "포천시"), new Region("경기도", "여주시"),
				new Region("경기도", "연천군"), new Region("경기도", "가평군"), new Region("경기도", "양평군"), new Region("강원도", "춘천시"),
				new Region("강원도", "원주시"), new Region("강원도", "강릉시"), new Region("강원도", "동해시"), new Region("강원도", "태백시"),
				new Region("강원도", "속초시"), new Region("강원도", "삼척시"), new Region("강원도", "홍천군"), new Region("강원도", "횡성군"),
				new Region("강원도", "영월군"), new Region("강원도", "평창군"), new Region("강원도", "정선군"), new Region("강원도", "철원군"),
				new Region("강원도", "화천군"), new Region("강원도", "양구군"), new Region("강원도", "인제군"), new Region("강원도", "고성군"),
				new Region("강원도", "양양군"), new Region("충청북도", "청주시 상당구"), new Region("충청북도", "청주시 서원구"),
				new Region("충청북도", "청주시 흥덕구"), new Region("충청북도", "청주시 청원구"), new Region("충청북도", "충주시"),
				new Region("충청북도", "제천시"), new Region("충청북도", "보은군"), new Region("충청북도", "옥천군"),
				new Region("충청북도", "영동군"), new Region("충청북도", "증평군"), new Region("충청북도", "진천군"),
				new Region("충청북도", "괴산군"), new Region("충청북도", "음성군"), new Region("충청북도", "단양군"),
				new Region("충청남도", "천안시 동남구"), new Region("충청남도", "천안시 서북구"), new Region("충청남도", "공주시"),
				new Region("충청남도", "보령시"), new Region("충청남도", "아산시"), new Region("충청남도", "서산시"),
				new Region("충청남도", "논산시"), new Region("충청남도", "계룡시"), new Region("충청남도", "당진시"),
				new Region("충청남도", "금산군"), new Region("충청남도", "부여군"), new Region("충청남도", "서천군"),
				new Region("충청남도", "청양군"), new Region("충청남도", "홍성군"), new Region("충청남도", "예산군"),
				new Region("충청남도", "태안군"), new Region("전라북도", "전주시 완산구"), new Region("전라북도", "전주시 덕진구"),
				new Region("전라북도", "군산시"), new Region("전라북도", "익산시"), new Region("전라북도", "정읍시"),
				new Region("전라북도", "남원시"), new Region("전라북도", "김제시"), new Region("전라북도", "완주군"),
				new Region("전라북도", "진안군"), new Region("전라북도", "무주군"), new Region("전라북도", "장수군"),
				new Region("전라북도", "임실군"), new Region("전라북도", "순창군"), new Region("전라북도", "고창군"),
				new Region("전라북도", "부안군"), new Region("전라남도", "목포시"), new Region("전라남도", "여수시"),
				new Region("전라남도", "순천시"), new Region("전라남도", "나주시"), new Region("전라남도", "광양시"),
				new Region("전라남도", "담양군"), new Region("전라남도", "곡성군"), new Region("전라남도", "구례군"),
				new Region("전라남도", "고흥군"), new Region("전라남도", "보성군"), new Region("전라남도", "화순군"),
				new Region("전라남도", "장흥군"), new Region("전라남도", "강진군"), new Region("전라남도", "해남군"),
				new Region("전라남도", "영암군"), new Region("전라남도", "무안군"), new Region("전라남도", "함평군"),
				new Region("전라남도", "영광군"), new Region("전라남도", "장성군"), new Region("전라남도", "완도군"),
				new Region("전라남도", "진도군"), new Region("전라남도", "신안군"), new Region("경상북도", "포항시 남구"),
				new Region("경상북도", "포항시 북구"), new Region("경상북도", "경주시"), new Region("경상북도", "김천시"),
				new Region("경상북도", "안동시"), new Region("경상북도", "구미시"), new Region("경상북도", "영주시"),
				new Region("경상북도", "영천시"), new Region("경상북도", "상주시"), new Region("경상북도", "문경시"),
				new Region("경상북도", "경산시"), new Region("경상북도", "군위군"), new Region("경상북도", "의성군"),
				new Region("경상북도", "청송군"), new Region("경상북도", "영양군"), new Region("경상북도", "영덕군"),
				new Region("경상북도", "청도군"), new Region("경상북도", "고령군"), new Region("경상북도", "성주군"),
				new Region("경상북도", "칠곡군"), new Region("경상북도", "예천군"), new Region("경상북도", "봉화군"),
				new Region("경상북도", "울진군"), new Region("경상북도", "울릉군"), new Region("경상남도", "창원시 의창구"),
				new Region("경상남도", "창원시 성산구"), new Region("경상남도", "창원시 마산합포구"), new Region("경상남도", "창원시 마산회원구"),
				new Region("경상남도", "창원시 진해구"), new Region("경상남도", "진주시"), new Region("경상남도", "통영시"),
				new Region("경상남도", "사천시"), new Region("경상남도", "김해시"), new Region("경상남도", "밀양시"),
				new Region("경상남도", "거제시"), new Region("경상남도", "양산시"), new Region("경상남도", "의령군"),
				new Region("경상남도", "함안군"), new Region("경상남도", "창녕군"), new Region("경상남도", "고성군"),
				new Region("경상남도", "남해군"), new Region("경상남도", "하동군"), new Region("경상남도", "산청군"),
				new Region("경상남도", "함양군"), new Region("경상남도", "거창군"), new Region("경상남도", "합천군"),
				new Region("제주특별자치도", "제주시"), new Region("제주특별자치도", "서귀포시") };
		for (Region region : regionList) {
			if (!regionRepository.existsByMainRegionAndSubRegion(region.getMainRegion(), region.getSubRegion())) {
				region.setPosition((int) regionRepository.count());
				regionRepository.save(region);
			}
		}
	}
}
