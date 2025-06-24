package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.ChatType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatMessageResponseDto {
    private Long squadId;
    private Long senderId;
    private String message;
    private List<String> imageUrls;
    private ChatType type;
    private boolean isMine;

}
