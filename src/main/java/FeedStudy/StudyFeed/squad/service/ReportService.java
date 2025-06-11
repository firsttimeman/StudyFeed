package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.squad.dto.ReportRequest;
import FeedStudy.StudyFeed.squad.entity.ReportFeed;
import FeedStudy.StudyFeed.squad.entity.ReportSquad;
import FeedStudy.StudyFeed.squad.entity.ReportUser;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.repository.ReportUserRepository;
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
    private final SquadRepository squadRepository;
    private final FeedRepository feedRepository;

    public void reportUser(Long reporterId, Long reportedId, ReportRequest req) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        User reported = userRepository.findById(reportedId).orElseThrow();
        ReportUser report = new ReportUser(reporter, reported, req.getCategory(), req.getContent());
        reported.increaseReportCount();
        reportUserRepository.save(report);

    }

    public void reportSquad(Long reporterId, Long squadId, ReportRequest req) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        Squad squad = squadRepository.findById(squadId).orElseThrow();
        ReportSquad report = new ReportSquad(squad, reporter, req.getCategory(), req.getContent());
        squad.increaseReportCount();
    }

    public void reportFeed(Long reporterId, Long feedId, ReportRequest req) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        Feed feed = feedRepository.findById(feedId).orElseThrow();
        ReportFeed report = new ReportFeed(reporter, feed, req.getCategory(), req.getContent());
    }
}
