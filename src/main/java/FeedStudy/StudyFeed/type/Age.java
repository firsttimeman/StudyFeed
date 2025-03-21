package FeedStudy.StudyFeed.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Age {
    TEEN("10대"),      // 10대 10 ~ 19
    TWENTIES("20대"),     // 20대
    THIRTIES("30대"),     // 30대
    FORTIES("40대"),      // 40대
    FIFTIES("50대"),      // 50대 이상
    ALL("연령무관");         // 연령 무관



    private final String description;
}
