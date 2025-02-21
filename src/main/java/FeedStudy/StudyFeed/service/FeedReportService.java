package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.entity.Feed;
import FeedStudy.StudyFeed.entity.FeedReport;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.exception.ErrorCode;
import FeedStudy.StudyFeed.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.repository.FeedReportRepository;
import FeedStudy.StudyFeed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedReportService {
    private final FeedReportRepository feedReportRepository;
    private final FeedRepository feedRepository;

    public boolean reportFeed(User user, Long feedId, String reason) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        if(feedReportRepository.existsByUserAndFeed(user,feed)) {
            throw new FeedException(ErrorCode.ALREADY_REPORTED);
        }

        feedReportRepository.save(new FeedReport(user, feed, reason));
        feed.increaseReportCount();
        return true;
    }

    public boolean unReportFeed(User user, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        FeedReport feedReport = feedReportRepository.findByUserAndFeed(user, feed)
                .orElseThrow(() -> new FeedException(ErrorCode.REPORT_NOT_FOUND));

        feedReportRepository.delete(feedReport);
        feed.decreaseReportCount();
        return true;
    }
}
