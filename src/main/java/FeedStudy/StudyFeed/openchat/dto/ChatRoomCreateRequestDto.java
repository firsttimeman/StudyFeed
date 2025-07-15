package FeedStudy.StudyFeed.openchat.dto;

import FeedStudy.StudyFeed.global.type.Topic;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomCreateRequestDto {

    private String title;
    private String description;
    private Topic topic;
    private int maxParticipants;
}
