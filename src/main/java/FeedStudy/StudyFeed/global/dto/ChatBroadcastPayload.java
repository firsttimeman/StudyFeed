package FeedStudy.StudyFeed.global.dto;

import FeedStudy.StudyFeed.global.type.ChatCategory;
import FeedStudy.StudyFeed.global.type.ChatType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatBroadcastPayload {
    private ChatCategory chatCategory;
    private Long roomId;
    private Long chatId;
    private Long senderId;
    private String message;
    private List<String> imageUrls;
    private ChatType type;
    private String nickname;
    private String profileImage;
    private String originNode;
}