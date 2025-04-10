package FeedStudy.StudyFeed.squad.dto;

import FeedStudy.StudyFeed.squad.entity.SquadChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SquadMessageDto {

    private Long squadMessageId;

    private String senderName;

    private String content;

    private LocalDateTime timeStamp;


    public static SquadMessageDto toDto(SquadChatMessage message) {
        Long squadMessageId = message.getId();
        String senderName = message.getSender().getNickName();
        String content = message.getContent();
        LocalDateTime timeStamp = message.getCreateTime();

        return new SquadMessageDto(squadMessageId, senderName, content, timeStamp);

    }
}
