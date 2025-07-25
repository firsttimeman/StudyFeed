package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.squad.entity.SquadChat;
import FeedStudy.StudyFeed.squad.service.SquadChatService;
import FeedStudy.StudyFeed.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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


    @Operation(
            summary = "채팅 메시지 조회",
            description = "특정 스쿼드의 최신 채팅 메시지를 조회하거나, 특정 메시지 ID 이전의 메시지들을 조회합니다."
    )
    @GetMapping("/messages")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SquadChat>> getMessages(@Parameter(hidden = true) @AuthenticationPrincipal User user,
    @Parameter(description = "스쿼드 ID", example = "1") @PathVariable Long squadId,
    @Parameter(description = "조회 기준 메시지 ID (이전 메시지 조회 시 사용)", example = "100")
    @RequestParam(value = "before", required = false) Long beforeId) {

        List<SquadChat> result = (beforeId == null) ? squadChatService.loadRecentMessages(squadId) :
                squadChatService.loadPreviousMessages(squadId, beforeId);

        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "채팅 이미지 전송",
            description = "스쿼드 채팅에 이미지를 전송하고 이미지 URL 목록을 반환합니다."
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> sendImage( @Parameter(hidden = true) @AuthenticationPrincipal User user,
                                                   @Parameter(description = "스쿼드 ID", example = "1") @PathVariable Long squadId,
                                                   @Parameter(description = "업로드할 이미지 파일 목록", required = true,
                                                           content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                                                   schema = @Schema(type = "array", format = "binary")))
                                                       @RequestParam List<MultipartFile> images) {

        List<String> imageUrls = squadChatService.uploadImagesAndReturnUrls(squadId, user.getId(), images);
        return ResponseEntity.ok(imageUrls);
    }
}
