package FeedStudy.StudyFeed.global.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class NoticeResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDate createdAt;
    private boolean isVisible;
    private LocalDate publishDate;
}