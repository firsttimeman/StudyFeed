package FeedStudy.StudyFeed.notice.service;

import FeedStudy.StudyFeed.notice.dto.NoticeRequestDto;
import FeedStudy.StudyFeed.notice.dto.NoticeResponseDto;
import FeedStudy.StudyFeed.notice.entity.Notice;
import FeedStudy.StudyFeed.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public NoticeResponseDto createNotice(NoticeRequestDto dto) {
        Notice notice = new Notice(dto.getTitle(), dto.getContent());
        Notice save = noticeRepository.save(notice);
        return NoticeResponseDto.toDto(save);
    }

    public List<NoticeResponseDto> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(notice -> NoticeResponseDto.toDto(notice))
                .toList();
    }

    public NoticeResponseDto getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id).orElseThrow();
        return NoticeResponseDto.toDto(notice);
    }

    @Transactional
    public NoticeResponseDto updateNotice(Long id, NoticeRequestDto dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        return NoticeResponseDto.toDto(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));
        noticeRepository.delete(notice);
    }



}
