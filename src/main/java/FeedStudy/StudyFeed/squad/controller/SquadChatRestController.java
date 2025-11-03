package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.global.jwt.UserPrincipal;
import FeedStudy.StudyFeed.squad.dto.ChatPageResponse;
import FeedStudy.StudyFeed.squad.service.SquadChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/squad/{squadId}/chat")
@RequiredArgsConstructor
public class SquadChatRestController {

    private final SquadChatService squadChatService;

    @Operation(summary = "채팅 메시지 조회", description = "해당 스쿼드의 최근 메시지 또는 특정 메시지 ID 이전의 메시지를 조회합니다.")
    @GetMapping("/messages")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChatPageResponse> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "스쿼드 ID") @PathVariable Long squadId,
            @Parameter(description = "이전 메시지 조회 기준 ID") @RequestParam(value = "before", required = false) Long beforeId,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size
    ) {
        ChatPageResponse page = squadChatService.getMessagesPage(squadId, principal.getUser().getId(), beforeId, size);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "이미지 업로드", description = "채팅방에 이미지를 업로드하고 S3 URL 목록을 반환합니다.")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<String>> sendImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "스쿼드 ID") @PathVariable Long squadId,
            @Parameter(description = "업로드할 이미지 파일들") @RequestPart("images") List<MultipartFile> images
    ) {
        List<String> urls = squadChatService.uploadImagesAndReturnUrls(squadId, principal.getUser().getId(), images);
        return ResponseEntity.ok(urls);
    }
}