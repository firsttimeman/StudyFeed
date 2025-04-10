package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.squad.entity.SquadChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SquadMessageRequest {

    private Long squadId;

    private Long senderId;

    private String content;

    private LocalDateTime timeStamp;


    public static SquadMessageRequest toDto(SquadChatMessage message) {
        Long squadId = message.getId();
        Long senderId = message.getSender().getId();
        String content = message.getContent();
        LocalDateTime timeStamp = message.getCreateTime();

        return new SquadMessageRequest(squadId, senderId, content, timeStamp);
    }
}
