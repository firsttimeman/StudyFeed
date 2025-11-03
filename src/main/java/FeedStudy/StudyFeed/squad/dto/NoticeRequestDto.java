package FeedStudy.StudyFeed.squad.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class NoticeRequestDto {
    @NotNull(message = "targetChatId 는 필수입니다.")
    private Long targetChatId;
}