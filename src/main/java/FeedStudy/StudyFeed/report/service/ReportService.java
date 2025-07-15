package FeedStudy.StudyFeed.report.service;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.report.repository.ReportContentRepository;
import FeedStudy.StudyFeed.report.dto.ReportRequest;
import FeedStudy.StudyFeed.report.entity.ReportContent;
import FeedStudy.StudyFeed.report.entity.ReportUser;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.report.repository.ReportUserRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {


    private final UserRepository userRepository;
    private final ReportUserRepository reportUserRepository;
    private final ReportContentRepository reportContentRepository;
    private final FeedRepository feedRepository;
    private final SquadRepository squadRepository;


    public void reportUser(Long reporterId, Long reportedId, ReportRequest req) {
        User reporter = findUser(reporterId);
        User reported = findUser(reportedId);

        ReportUser report = new ReportUser(reporter, reported, req.getReportReasonUser(), req.getContent());
        reported.increaseReportCount();
        reportUserRepository.save(report);

    }


    public void reportFeed(Long reporterId, Long feedId, ReportRequest req) {
        User reporter = findUser(reporterId);
        Feed feed = feedRepository.findById(feedId).orElseThrow();

        ReportContent report = ReportContent.ofFeed(reporter, feed, req.getReportReasonContent(), req.getContent());
        reportContentRepository.save(report);
    }

    public void reportSquad(Long reporterId, Long squadId, ReportRequest req) {
        User reporter = findUser(reporterId);
        Squad squad = squadRepository.findById(squadId).orElseThrow();

        ReportContent report = ReportContent.ofSquad(reporter, squad, req.getReportReasonContent(), req.getContent());
        squad.increaseReportCount();
        reportContentRepository.save(report);
    }


    private User findUser(Long reportedId) {
        return userRepository.findById(reportedId).orElseThrow();
    }
}
