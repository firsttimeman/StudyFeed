package FeedStudy.StudyFeed.openchat.controller;

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

@RestController
@RequestMapping("/api/chat/{roomId}")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@AuthenticationPrincipal User user,
                                                         @PathVariable Long roomId,
                                                         @RequestParam(value = "before", required = false) Long beforeId) {
        List<ChatMessage> result = (beforeId == null)
                ? chatService.loadRecentMessages(roomId, PageRequest.of(0, 20))
                : chatService.loadPreviousMessages(roomId, beforeId, PageRequest.of(0, 20));
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadImages(@AuthenticationPrincipal User user,
                                                     @PathVariable Long roomId,
                                                     @RequestParam List<MultipartFile> images) {
        List<String> urls = chatService.uploadImagesAndReturnUrls(roomId, user.getId(), images);
        return ResponseEntity.ok(urls);
    }
}
