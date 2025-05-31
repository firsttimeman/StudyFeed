package FeedStudy.StudyFeed.global.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DataResponse {
    private List<?> list;
    private boolean hasNext;
}
