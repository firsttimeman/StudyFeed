package FeedStudy.StudyFeed.openchat.dto;

import FeedStudy.StudyFeed.global.type.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomCreateRequestDto {

    @Schema(description = "채팅방 제목")
    private String title;
    @Schema(description = "채팅방 설명")
    private String description;
    @Schema(description = "채팅방 주제")
    private Topic topic;
    @Schema(description = "채팅방 최대 참여 인원 설정")
    private int maxParticipants;
}
