package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.dto.RegionRequest;
import FeedStudy.StudyFeed.global.exception.ValidEnum;
import FeedStudy.StudyFeed.global.type.SquadAccessType;
import FeedStudy.StudyFeed.global.type.SquadGender;
import FeedStudy.StudyFeed.global.type.Topic;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SquadCreateRequestDto implements RegionRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String squadName;

    @ValidEnum(enumClass = Topic.class, message = "유효한 값을 입력하셔야 합니다.")
    @NotNull(message = "유효한 값을 넣으셔야 합니다.")
    private Topic topic; // enum 설정 필요


    @NotNull
    private String regionMain;

    private String regionSub;

    @NotNull
    private LocalDate meetDate;

    @NotNull
    private LocalTime meetTime;

    @Min(2)
    private int peopleNum;

    @NotNull
    @ValidEnum(enumClass = SquadGender.class, message = "모집 성별을 입력해주세요")
    private SquadGender squadGender;


//    private Age age;

    @Min(50)
    private Integer minAge;
    @Max(100)
    private Integer maxAge;

    @Size(max = 500)
    private String description;

    @ValidEnum(enumClass = SquadAccessType.class, message = "방식을 선택해주세요")
    @NotNull
    private SquadAccessType  squadAccessType;




}
