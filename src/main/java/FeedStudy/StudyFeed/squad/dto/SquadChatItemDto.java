package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.global.type.ChatType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SquadChatItemDto {
    private Long chatId;
    private Long senderId;
    private String nickname;
    private String profileImage;
    private ChatType type;
    private String message;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}