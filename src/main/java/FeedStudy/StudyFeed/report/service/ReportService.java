package FeedStudy.StudyFeed.report.service;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.ReportException;
import FeedStudy.StudyFeed.report.dto.ReportRequest;
import FeedStudy.StudyFeed.report.entity.ReportContent;
import FeedStudy.StudyFeed.report.entity.ReportUser;
import FeedStudy.StudyFeed.report.repository.ReportContentRepository;
import FeedStudy.StudyFeed.report.repository.ReportUserRepository;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportUserRepository reportUserRepository;
    private final ReportContentRepository reportContentRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final SquadRepository squadRepository;

    public void reportUser(User reporter, Long reportedId, ReportRequest req) {
        User reported = userRepository.findById(reportedId)
                .orElseThrow(() -> new ReportException(ErrorCode.USER_NOT_FOUND));

        if (reporter.getId().equals(reported.getId())) {
            throw new ReportException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (req.getReportReasonUser() == null) {
            throw new ReportException(ErrorCode.INVALID_REPORT_REASON);
        }

        if (reportUserRepository.existsByReporterAndReported(reporter, reported)) {
            throw new ReportException(ErrorCode.ALREADY_REPORTED);
        }

        String content = normalizeContent(req.getContent());

        ReportUser report = new ReportUser(
                reporter,
                reported,
                req.getReportReasonUser(),
                content
        );

        userRepository.increaseReportCount(reported.getId());
        reportUserRepository.save(report);
    }

    public void reportFeed(User reporter, Long feedId, ReportRequest req) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ReportException(ErrorCode.FEED_NOT_FOUND));

        if (feed.getUser() != null && feed.getUser().getId().equals(reporter.getId())) {
            throw new ReportException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (req.getReportReasonContent() == null) {
            throw new ReportException(ErrorCode.INVALID_REPORT_REASON);
        }

        if (reportContentRepository.existsByReporterAndFeed(reporter, feed)) {
            throw new ReportException(ErrorCode.ALREADY_REPORTED);
        }

        String content = normalizeContent(req.getContent());

        ReportContent report = ReportContent.ofFeed(
                reporter,
                feed,
                req.getReportReasonContent(),
                content
        );

        feedRepository.increaseReportCount(feed.getId());
        reportContentRepository.save(report);
    }

    public void reportSquad(User reporter, Long squadId, ReportRequest req) {
        Squad squad = squadRepository.findById(squadId)
                .orElseThrow(() -> new ReportException(ErrorCode.SQUAD_NOT_FOUND));

        if (squad.getUser() != null && squad.getUser().getId().equals(reporter.getId())) {
            throw new ReportException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (req.getReportReasonContent() == null) {
            throw new ReportException(ErrorCode.INVALID_REPORT_REASON);
        }

        if (reportContentRepository.existsByReporterAndSquad(reporter, squad)) {
            throw new ReportException(ErrorCode.ALREADY_REPORTED);
        }

        String content = normalizeContent(req.getContent());

        ReportContent report = ReportContent.ofSquad(
                reporter,
                squad,
                req.getReportReasonContent(),
                content
        );

        squadRepository.increaseReportCount(squad.getId());
        reportContentRepository.save(report);
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }

        String trimmed = content.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}