package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.squad.entity.SquadChat;
import FeedStudy.StudyFeed.squad.service.SquadChatService;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/squad/{squadId}/chat")
@RequiredArgsConstructor
public class SquadChatRestController {


    private final SquadChatService squadChatService;


    @GetMapping("/messages")
    public ResponseEntity<List<SquadChat>> getMessages(@AuthenticationPrincipal User user, @PathVariable Long squadId
    , @RequestParam(value = "before", required = false) Long beforeId) {

        List<SquadChat> result = (beforeId == null) ? squadChatService.loadRecentMessages(squadId) : squadChatService.loadPreviousMessages(squadId, beforeId);

        return ResponseEntity.ok(result);
    }


    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> sendImage(@AuthenticationPrincipal User user,
                                               @PathVariable Long squadId,
                                               @RequestParam List<MultipartFile> images) {

        List<String> imageUrls = squadChatService.uploadImagesAndReturnUrls(squadId, user.getId(), images);
        return ResponseEntity.ok(imageUrls);
    }
}
