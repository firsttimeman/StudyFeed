package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.global.dto.NoticeRequest;
import FeedStudy.StudyFeed.global.dto.NoticeResponse;
import FeedStudy.StudyFeed.global.entity.Notice;
import FeedStudy.StudyFeed.global.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeResponse createNotice(NoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .visible(request.isVisible())
                .build();

        return toResponse(noticeRepository.save(notice));
    }

    public List<NoticeResponse> getVisibleNotices() { // 사용자 공개용
        return  noticeRepository.findVisibleNotices(LocalDate.now())
                .stream()
                .map(notice -> toResponse(notice))
                .toList();
    }

    public List<NoticeResponse> getAllNoticesForAdmin() { // 관리자 전체 공지 보기
        return noticeRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(n -> toResponse(n))
                .toList();
    }

    public List<NoticeResponse> getHiddenNotices() {
        return noticeRepository.findAllByVisibleFalseOrderByCreatedAtDesc()
                .stream().map(this::toResponse)
                .toList();
    }

    public NoticeResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        return toResponse(notice);
    }

    public NoticeResponse updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setVisible(request.isVisible());
        return toResponse(noticeRepository.save(notice));
    }

    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    private NoticeResponse toResponse(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(LocalDate.from(notice.getCreatedAt()))
                .isVisible(notice.isVisible())
                .build();
    }
}