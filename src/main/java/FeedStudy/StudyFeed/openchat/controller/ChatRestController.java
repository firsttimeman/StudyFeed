package FeedStudy.StudyFeed.openchat.controller;

import FeedStudy.StudyFeed.openchat.dto.ChatRoomCreateRequestDto;
import FeedStudy.StudyFeed.openchat.dto.ChatRoomCreateResponseDto;
import FeedStudy.StudyFeed.openchat.entity.ChatMessage;
import FeedStudy.StudyFeed.openchat.service.ChatService;
import FeedStudy.StudyFeed.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Open Chat API", description = "오픈 채팅 관련 REST API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @Operation(summary = "채팅 메시지 조회", description = "채팅방의 최신 메시지를 조회하거나 특정 메시지 이전의 메시지를 조회합니다.")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@AuthenticationPrincipal User user,
                                                         @PathVariable Long roomId,
                                                         @RequestParam(value = "before", required = false) Long beforeId) {
        List<ChatMessage> result = (beforeId == null)
                ? chatService.loadRecentMessages(roomId, PageRequest.of(0, 20))
                : chatService.loadPreviousMessages(roomId, beforeId, PageRequest.of(0, 20));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "채팅방 이미지 업로드", description = "채팅방에 이미지를 업로드합니다.")
    @PostMapping(value = "{roomId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadImages(@AuthenticationPrincipal User user,
                                                     @Parameter(description = "채팅방 ID", example = "1")
                                                     @PathVariable Long roomId,
                                                     @Parameter(description = "업로드할 이미지 파일 목록")
                                                         @RequestParam List<MultipartFile> images) {
        List<String> urls = chatService.uploadImagesAndReturnUrls(roomId, user.getId(), images);
        return ResponseEntity.ok(urls);
    }


    @Operation(summary = "채팅방 생성", description = "새로운 오픈 채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<?> createChatRoom(@AuthenticationPrincipal User user,
                                            @RequestBody ChatRoomCreateRequestDto dto) {
        ChatRoomCreateResponseDto chatRoom = chatService.createChatRoom(user.getId(), dto);
        return ResponseEntity.ok(chatRoom);
    }

    @Operation(summary = "채팅방 참여", description = "오픈 채팅방에 참여하고 토큰을 발급받습니다.")
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinChatRoom(@AuthenticationPrincipal User user,
                                          @Parameter(description = "채팅방 ID", example = "1")
                                          @PathVariable Long roomId) {
        Map<String, String> token = chatService.joinChatRoomWithToken(roomId, user.getId());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "채팅방 나가기", description = "오픈 채팅방에서 나갑니다.")
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveChatRoom(@AuthenticationPrincipal User user,
                                           @Parameter(description = "채팅방 ID", example = "1")
                                           @PathVariable Long roomId) {
        chatService.leaveChatRoom(roomId, user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "채팅방 벤기능", description = "오픈 채팅방에서 사용자를 벤합니다.")
    @PostMapping("/{roomId}/kick")
    public ResponseEntity<?> kickParticipants(@AuthenticationPrincipal User user,
                                              @Parameter(description = "채팅방 ID", example = "1")
                                              @PathVariable Long roomId) {
        chatService.kickParticipant(roomId, user);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "채팅 토큰 갱신", description = "채팅방에 사용되는 JWT 토큰을 재발급합니다.")
    @GetMapping("{roomId}/refresh-token")
    public ResponseEntity<?> refreshChatToken(@AuthenticationPrincipal User user,
                                              @Parameter(description = "채팅방 ID", example = "1")
                                              @PathVariable Long roomId) {
        Map<String, String> token = chatService.refreshChatToken(roomId, user);
        return ResponseEntity.ok(token);
    }
}
