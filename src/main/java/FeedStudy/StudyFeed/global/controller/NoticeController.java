package FeedStudy.StudyFeed.global.controller;

import FeedStudy.StudyFeed.global.dto.NoticeRequest;
import FeedStudy.StudyFeed.global.dto.NoticeResponse;
import FeedStudy.StudyFeed.global.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/notice")
@RequiredArgsConstructor
@Tag(name = "공지사항", description = "공지사항 관련 API")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 등록", description = "관리자가 새로운 공지를 등록합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoticeResponse> createNotice(@RequestBody NoticeRequest request) {
        return ResponseEntity.ok(noticeService.createNotice(request));
    }

    @Operation(summary = "공개 공지 목록 조회", description = "사용자에게 노출되는 공지 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getVisibleNotices() {
        return ResponseEntity.ok(noticeService.getVisibleNotices());
    }

    @Operation(summary = "전체 공지 목록 조회 (관리자)", description = "관리자가 모든 공지를 조회합니다.")
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NoticeResponse>> getAllNoticesForAdmin() {
        return ResponseEntity.ok(noticeService.getAllNoticesForAdmin());
    }

    @Operation(summary = "숨겨진 공지 목록 조회 (관리자)", description = "관리자가 비공개 공지 목록을 조회합니다.")
    @GetMapping("/admin/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NoticeResponse>> getHiddenNotices() {
        return ResponseEntity.ok(noticeService.getHiddenNotices());
    }

    @Operation(summary = "공지 상세 조회", description = "공지사항 ID로 상세 내용을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNoticeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(id));
    }

    @Operation(summary = "공지 수정", description = "관리자가 기존 공지사항을 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeRequest request) {
        return ResponseEntity.ok(noticeService.updateNotice(id, request));
    }

    @Operation(summary = "공지 삭제", description = "관리자가 공지사항을 삭제합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }


}
