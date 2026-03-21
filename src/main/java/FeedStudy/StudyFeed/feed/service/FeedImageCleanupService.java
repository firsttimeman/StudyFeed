package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.feed.dto.S3TempFileInfo;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.global.service.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
//todo 나중에 이해하기
public class FeedImageCleanupService {

    private static final Duration ORPHAN_FILE_THRESHOLD = Duration.ofHours(24);

    private final S3FileService s3FileService;
    private final FeedImageRepository feedImageRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOrphanTempImages() {
        log.info("orphan temp image cleanup 시작");

        Instant cutoff = Instant.now().minus(ORPHAN_FILE_THRESHOLD);

        List<S3TempFileInfo> tempFiles = s3FileService.findTempFiles();
        Set<String> usedImageUrls = feedImageRepository.findAllImageUrls().stream()
                .collect(Collectors.toSet());

        int deletedCount = 0;
        int skippedCount = 0;

        for (S3TempFileInfo tempFile : tempFiles) {
            boolean tooRecent = tempFile.getLastModified().isAfter(cutoff);
            boolean usedInDb = usedImageUrls.contains(tempFile.getUrl());

            if (tooRecent || usedInDb) {
                skippedCount++;
                continue;
            }

            try {
                s3FileService.delete(tempFile.getKey());
                deletedCount++;
                log.info("orphan temp image 삭제 완료. key={}, url={}", tempFile.getKey(), tempFile.getUrl());
            } catch (Exception e) {
                log.warn("orphan temp image 삭제 실패. key={}, url={}", tempFile.getKey(), tempFile.getUrl(), e);
            }
        }

        log.info("orphan temp image cleanup 종료. deletedCount={}, skippedCount={}", deletedCount, skippedCount);
    }

}
