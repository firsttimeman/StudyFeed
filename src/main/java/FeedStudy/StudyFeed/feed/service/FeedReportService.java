package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedReport;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.feed.repository.FeedReportRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
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


}
