package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.global.jwt.UserPrincipal;
import FeedStudy.StudyFeed.squad.dto.*;
import FeedStudy.StudyFeed.squad.entity.SquadChat;
import FeedStudy.StudyFeed.squad.service.SquadChatService;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import FeedStudy.StudyFeed.squad.entity.SquadChatImage;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SquadChatController {
    private final SquadChatService squadChatService;
    private final SimpMessagingTemplate simpMessagingTemplate;
//
//    @MessageMapping("/squadChat.sendMessage")
////    @SendTo("/topic/squad/{squadId}") // 이거 동적으로 안됨 정직하게 {squadid}이런식으로 됨
//    public void sendMessage(SquadMessageRequest request) {
//        SquadMessageDto dto = squadChatService.saveMessage(request);
//        simpMessagingTemplate.convertAndSend("/topic/squad/" + request.getSquadId(), dto);
//    }


    @MessageMapping("/squad/{squadId}")
    public void sendTextMessage(@DestinationVariable Long squadId, TextMessageDto dto, Principal principal) {
        User user = extractUserFromPrincipal(principal);
        SquadChat chat = squadChatService.sendTextMessage(squadId, user.getId(), dto.getMessage());
        simpMessagingTemplate.convertAndSend("/sub/squad/" + squadId, toResponseDto(user, chat));
    }

    @MessageMapping("/squad/{squadId}/image")
    public void sendImageMessage(@DestinationVariable Long squadId, ImageMessageDto dto, Principal principal) {
        User user = extractUserFromPrincipal(principal);
        SquadChat chat = squadChatService.sendImageMessage(squadId, user.getId(), dto.getImageUrls());
        simpMessagingTemplate.convertAndSend("/sub/squad/" + squadId, toResponseDto(user, chat));
    }

    @MessageMapping("/squad/{squadId}/notice")
    public void sendNotice(@DestinationVariable Long squadId,
            NoticeRequestDto dto,
                           Principal principal) {
        User user = extractUserFromPrincipal(principal);
        SquadChat noticeChat = squadChatService.postNotice(squadId, user.getId(), dto.getTargetChatId());
        simpMessagingTemplate.convertAndSend("/sub/squad/" + squadId, toResponseDto(user, noticeChat));
    }

    @MessageMapping("/squad/{squadId}/delete")
    public void deleteChatMessage(@DestinationVariable Long squadId,
                                  SquadChatDeleteDto dto,
                                  Principal principal) {
        User user = extractUserFromPrincipal(principal);
        SquadChat squadChat = squadChatService.deleteMessage(dto.getChatId(), user.getId());
        simpMessagingTemplate.convertAndSend("/sub/squad/" + squadId, toResponseDto(user, squadChat));
        //todo 물어보기
    }


    private User extractUserFromPrincipal(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            Object authPrincipal = token.getPrincipal();
            if (authPrincipal instanceof UserPrincipal userPrincipal) {
                return userPrincipal.getUser();
            }
        }
        throw new IllegalArgumentException("Invalid principal");
    }



    private ChatMessageResponseDto toResponseDto(User user, SquadChat chat) {
        List<String> imageUrls = chat.getImages().stream().map(SquadChatImage::getUrl).toList();

        return ChatMessageResponseDto.builder()
                .squadId(chat.getSquad().getId())
                .senderId(chat.getUser().getId())
                .message(chat.getMessage())
                .imageUrls(imageUrls)
                .type(chat.getType())
                .isMine(user.getId().equals(chat.getUser().getId()))
                .deletable("삭제된 메세지 입니다.".equals(chat.getMessage()))
                .build();
    }



}
