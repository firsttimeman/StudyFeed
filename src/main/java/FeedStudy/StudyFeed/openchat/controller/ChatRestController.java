package FeedStudy.StudyFeed.openchat.controller;

import FeedStudy.StudyFeed.openchat.dto.ChatRoomCreateRequestDto;
import FeedStudy.StudyFeed.openchat.dto.ChatRoomCreateResponseDto;
import FeedStudy.StudyFeed.openchat.entity.ChatMessage;
import FeedStudy.StudyFeed.openchat.service.ChatService;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@AuthenticationPrincipal User user,
                                                         @PathVariable Long roomId,
                                                         @RequestParam(value = "before", required = false) Long beforeId) {
        List<ChatMessage> result = (beforeId == null)
                ? chatService.loadRecentMessages(roomId, PageRequest.of(0, 20))
                : chatService.loadPreviousMessages(roomId, beforeId, PageRequest.of(0, 20));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "{roomId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadImages(@AuthenticationPrincipal User user,
                                                     @PathVariable Long roomId,
                                                     @RequestParam List<MultipartFile> images) {
        List<String> urls = chatService.uploadImagesAndReturnUrls(roomId, user.getId(), images);
        return ResponseEntity.ok(urls);
    }

    @PostMapping
    public ResponseEntity<?> createChatRoom(@AuthenticationPrincipal User user,
                                            @RequestBody ChatRoomCreateRequestDto dto) {
        ChatRoomCreateResponseDto chatRoom = chatService.createChatRoom(user.getId(), dto);
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinChatRoom(@AuthenticationPrincipal User user,
                                          @PathVariable Long roomId) {
        Map<String, String> token = chatService.joinChatRoomWithToken(roomId, user.getId());
        return ResponseEntity.ok(token);
    }

    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveChatRoom(@AuthenticationPrincipal User user, @PathVariable Long roomId) {
        chatService.leaveChatRoom(roomId, user.getId());
        return ResponseEntity.ok().build();
    }


    @GetMapping("{roomId}/refresh-token")
    public ResponseEntity<?> refreshChatToken(@AuthenticationPrincipal User user,
                                              @PathVariable Long roomId) {
        Map<String, String> token = chatService.refreshChatToken(roomId, user);
        return ResponseEntity.ok(token);
    }
}
