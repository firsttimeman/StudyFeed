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

    public void reportUser(Long reporterId, Long reportedId, ReportRequest req) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ReportException(ErrorCode.USER_NOT_FOUND));
        User reported = userRepository.findById(reportedId)
                .orElseThrow(() -> new ReportException(ErrorCode.USER_NOT_FOUND));

        if (reportUserRepository.existsByReporterAndReported(reporter, reported))
            throw new ReportException(ErrorCode.ALREADY_REPORTED);

        ReportUser report = new ReportUser(reporter, reported, req.getReportReasonUser(), req.getContent());
        userRepository.increaseReportCount(reported.getId());
        reportUserRepository.save(report);
    }

    public void reportFeed(Long reporterId, Long feedId, ReportRequest req) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ReportException(ErrorCode.USER_NOT_FOUND));
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ReportException(ErrorCode.FEED_NOT_FOUND));

        if (reportContentRepository.existsByReporterAndFeed(reporter, feed))
            throw new ReportException(ErrorCode.ALREADY_REPORTED);

        ReportContent report = ReportContent.ofFeed(reporter, feed, req.getReportReasonContent(), req.getContent());
        feedRepository.increaseReportCount(feed.getId());
        reportContentRepository.save(report);
    }

    public void reportSquad(Long reporterId, Long squadId, ReportRequest req) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ReportException(ErrorCode.USER_NOT_FOUND));
        Squad squad = squadRepository.findById(squadId)
                .orElseThrow(() -> new ReportException(ErrorCode.SQUAD_NOT_FOUND));

        if (reportContentRepository.existsByReporterAndSquad(reporter, squad))
            throw new ReportException(ErrorCode.ALREADY_REPORTED);

        ReportContent report = ReportContent.ofSquad(reporter, squad, req.getReportReasonContent(), req.getContent());
        squadRepository.increaseReportCount(squad.getId());
        reportContentRepository.save(report);
    }
}