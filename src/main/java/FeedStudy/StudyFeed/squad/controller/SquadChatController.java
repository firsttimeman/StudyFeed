package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.global.jwt.UserPrincipal;
import FeedStudy.StudyFeed.squad.dto.ImageMessageDto;
import FeedStudy.StudyFeed.squad.dto.NoticeRequestDto;
import FeedStudy.StudyFeed.squad.dto.SquadChatDeleteDto;
import FeedStudy.StudyFeed.squad.dto.TextMessageDto;
import FeedStudy.StudyFeed.squad.service.SquadChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SquadChatController {

    private final SquadChatService squadChatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 텍스트 메시지
    @MessageMapping("/squad/{squadId}")
    public void sendTextMessage(@DestinationVariable Long squadId,
                                @Valid TextMessageDto dto,
                                @AuthenticationPrincipal UserPrincipal principal) {
        var user = principal.getUser();
        squadChatService.sendTextMessage(squadId, user.getId(), dto.getMessage());
    }

    // 이미지 메시지
    @MessageMapping("/squad/{squadId}/image")
    public void sendImageMessage(@DestinationVariable Long squadId,
                                 @Valid ImageMessageDto dto,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        var user = principal.getUser();
        squadChatService.sendImageMessage(squadId, user.getId(), dto.getImageUrls());
    }

    // 공지 등록
    @MessageMapping("/squad/{squadId}/notice")
    public void sendNotice(@DestinationVariable Long squadId,
                           @Valid NoticeRequestDto dto,
                           @AuthenticationPrincipal UserPrincipal principal) {
        var user = principal.getUser();
        squadChatService.postNotice(squadId, user.getId(), dto.getTargetChatId());
    }

    // 메시지 삭제
    @MessageMapping("/squad/{squadId}/delete")
    public void deleteChatMessage(@DestinationVariable Long squadId,
                                  @Valid SquadChatDeleteDto dto,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        var user = principal.getUser();
        squadChatService.deleteMessage(dto.getChatId(), user.getId());
    }

    /** STOMP 에러를 현재 사용자에게만 전달 (/user/queue/errors) */
    @MessageExceptionHandler
    public void handleException(Throwable ex, SimpMessageHeaderAccessor headers) {
        log.warn("[STOMP-ERROR] {}", ex.toString());
        String userName = headers.getUser() != null ? headers.getUser().getName() : null;
        if (userName != null) {
            messagingTemplate.convertAndSendToUser(
                    userName,
                    "/queue/errors",
                    ex.getMessage() != null ? ex.getMessage() : "Unknown error"
            );
        } else {
            log.warn("No user found in STOMP headers; cannot send error frame to specific user.");
        }
    }
}