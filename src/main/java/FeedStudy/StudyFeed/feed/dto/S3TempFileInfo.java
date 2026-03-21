package FeedStudy.StudyFeed.feed.dto;


import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3TempFileInfo {
    private String key;
    private String url;
    private Instant lastModified;
}
