package FeedStudy.StudyFeed.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NoticeRequest {

    @NotBlank(message = "공지 제목은 필수입니다.")
    @Size(max = 100, message = "공지 제목은 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "공지 내용은 필수입니다.")
    @Size(max = 2000, message = "공지 내용은 2000자 이하여야 합니다.")
    private String content;

    private boolean isVisible;

    private LocalDate publishDate;
}