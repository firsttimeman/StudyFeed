package FeedStudy.StudyFeed.openchat.dto;

import FeedStudy.StudyFeed.global.type.ChatType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ChatMessageResponseDto {
    private Long roomId;
    private Long senderId;
    private String nickname;          // 추가
    private String profileImageUrl;   // 추가
    private String content;
    private ChatType type;
    private List<String> imageUrls;
    private boolean isMine;
    private boolean deleted;
}
