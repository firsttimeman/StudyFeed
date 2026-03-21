package FeedStudy.StudyFeed.notice.service;

import FeedStudy.StudyFeed.notice.dto.NoticeRequest;
import FeedStudy.StudyFeed.notice.dto.NoticeResponse;
import FeedStudy.StudyFeed.notice.domain.Notice;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.NoticeException;
import FeedStudy.StudyFeed.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeResponse createNotice(NoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .visible(request.isVisible())
                .publishDate(request.getPublishDate())
                .build();

        return toResponse(noticeRepository.save(notice));
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> getVisibleNotices() {
        return noticeRepository.findVisibleNotices(LocalDate.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> getAllNoticesForAdmin() {
        return noticeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> getHiddenNotices() {
        return noticeRepository.findAllByVisibleFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));
        return toResponse(notice);
    }

    public NoticeResponse updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));

        notice.update(
                request.getTitle(),
                request.getContent(),
                request.isVisible(),
                request.getPublishDate()
        );

        return toResponse(notice);
    }

    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));

        noticeRepository.delete(notice);
    }

    private NoticeResponse toResponse(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt().toLocalDate())
                .isVisible(notice.isVisible())
                .publishDate(notice.getPublishDate())
                .build();
    }
}