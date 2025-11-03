package FeedStudy.StudyFeed.squad.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadChatPayload {
    private Long squadId;
    private Long senderId;
    private String message;
    private String senderNickname;
}