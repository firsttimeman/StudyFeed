package FeedStudy.StudyFeed.openchat.controller;

import FeedStudy.StudyFeed.global.jwt.UserPrincipal;
import FeedStudy.StudyFeed.openchat.dto.ChatDeleteRequestDto;
import FeedStudy.StudyFeed.openchat.dto.ChatImageRequestDto;
import FeedStudy.StudyFeed.openchat.dto.ChatMessageRequestDto;
import FeedStudy.StudyFeed.openchat.dto.ChatMessageResponseDto;
import FeedStudy.StudyFeed.openchat.entity.ChatMessage;
import FeedStudy.StudyFeed.openchat.service.ChatService;
import FeedStudy.StudyFeed.squad.dto.NoticeRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat/{roomId}")
    public void sendText(@DestinationVariable Long roomId, ChatMessageRequestDto dto, Principal principal) {
        User user = extractUser(principal);
        ChatMessage textMessage = chatService.createTextMessage(roomId, user.getId(), dto.getContent());
        simpMessagingTemplate.convertAndSend("/sub/chat/" + roomId, toResponse(user, textMessage));
    }

    @MessageMapping("/chat/{roomId}/image")
    public void sendImage(@DestinationVariable Long roomId, ChatImageRequestDto dto, Principal principal) {

        User user = extractUser(principal);
        ChatMessage message = chatService.sendImageMessage(roomId, user.getId(), dto.getImageUrls());
        simpMessagingTemplate.convertAndSend("/sub/chat/" + roomId, toResponse(user,message));
    }

    @MessageMapping("/chat/{roomId}/notice")
    public void sendNotice(@DestinationVariable Long roomId, NoticeRequestDto dto, Principal principal) {
        User user = extractUser(principal);
        ChatMessage notice = chatService.postNotice(roomId, user.getId(), dto.getTargetChatId());
        simpMessagingTemplate.convertAndSend("/sub/chat/" + roomId, toResponse(user, notice));
    }

    @MessageMapping("/chat/{roomId}/delete")
    public void deleteMessage(@DestinationVariable Long roomId, ChatDeleteRequestDto dto, Principal principal) {
        User user = extractUser(principal);
        ChatMessage message = chatService.deleteMessage(dto.getMessageId(), user.getId());
        simpMessagingTemplate.convertAndSend("/sub/chat/" + roomId, toResponse(user, message));
    }



    private User extractUser(Principal principal) {
        if(principal instanceof UsernamePasswordAuthenticationToken token) {
            Object principal1 = token.getPrincipal();
            if(principal1 instanceof UserPrincipal userPrincipal) {
                return userPrincipal.getUser();
            }
        }
        throw new IllegalArgumentException("인증 정보가 없습니다.");
    }

    private ChatMessageResponseDto toResponse(User user, ChatMessage message) {

        List<String> imageUrls = message.getImages().stream()
                .map(chatImage -> chatImage.getUrl())
                .toList();

        User sender = message.getSender();
        Long senderId = (sender != null) ? sender.getId() : null;
        String nickname = (sender != null) ? sender.getNickName() : "탈퇴한 회원입니다";
        String profileImage = (sender != null) ? sender.getImageUrl() : "avatar_placeholder.png";

        boolean isMine = (sender != null) && user.getId().equals(senderId);
        boolean deleted = "삭제된 메세지 입니다.".equals(message.getContent());


        return ChatMessageResponseDto.builder()
                .roomId(message.getChatRoom().getId())
                .senderId(senderId)
                .nickname(nickname)
                .profileImageUrl(profileImage)
                .content(message.getContent())
                .type(message.getType())
                .imageUrls(imageUrls)
                .isMine(isMine)
                .deleted(deleted)
                .build();
    }
}
