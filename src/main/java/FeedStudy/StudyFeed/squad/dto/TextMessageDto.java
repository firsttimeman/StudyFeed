package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.ChatType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class TextMessageDto {
    @NotBlank(message = "메시지를 입력하세요.")
    private String message;
}