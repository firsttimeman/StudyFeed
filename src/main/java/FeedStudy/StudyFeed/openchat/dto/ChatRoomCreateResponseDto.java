package FeedStudy.StudyFeed.openchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomCreateResponseDto {
    private Long roomId;
    private String title;
}
