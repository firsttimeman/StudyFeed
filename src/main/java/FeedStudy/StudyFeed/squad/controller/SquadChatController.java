package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.squad.dto.SquadMessageDto;
import FeedStudy.StudyFeed.squad.dto.SquadMessageRequest;
import FeedStudy.StudyFeed.squad.service.SquadChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SquadChatController {
    private final SquadChatService squadChatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/squadChat.sendMessage")
//    @SendTo("/topic/squad/{squadId}") // 이거 동적으로 안됨 정직하게 {squadid}이런식으로 됨
    public void sendMessage(SquadMessageRequest request) {
        SquadMessageDto dto = squadChatService.saveMessage(request);
        simpMessagingTemplate.convertAndSend("/topic/squad/" + request.getSquadId(), dto);
    }


}
