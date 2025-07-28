package FeedStudy.StudyFeed.global.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NoticeRequest {
    private String title;
    private String content;
    private boolean isVisible;
    private LocalDate publishDate;
}