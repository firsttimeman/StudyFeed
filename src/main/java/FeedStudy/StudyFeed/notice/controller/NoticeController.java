package FeedStudy.StudyFeed.notice.controller;

import FeedStudy.StudyFeed.notice.dto.NoticeRequestDto;
import FeedStudy.StudyFeed.notice.dto.NoticeResponseDto;
import FeedStudy.StudyFeed.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/create")
    public ResponseEntity<?> createNotice(@RequestBody NoticeRequestDto dto) {
        NoticeResponseDto notice = noticeService.createNotice(dto);
        return ResponseEntity.ok(notice);
    }

    @GetMapping("/allnotices")
    public ResponseEntity<?> getAllNotices() {
        List<NoticeResponseDto> allNotices = noticeService.getAllNotices();
        return ResponseEntity.ok(allNotices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotice(@PathVariable Long id) {
        NoticeResponseDto noticeById = noticeService.getNoticeById(id);
        return ResponseEntity.ok(noticeById);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> updateNotice(@PathVariable Long id,
                                                          @RequestBody @Valid NoticeRequestDto dto) {
        return ResponseEntity.ok(noticeService.updateNotice(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
