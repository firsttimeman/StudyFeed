package FeedStudy.StudyFeed.squad.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SquadChatDeleteDto {
    @NotNull(message = "chatId 는 필수입니다.")
    private Long chatId;
}