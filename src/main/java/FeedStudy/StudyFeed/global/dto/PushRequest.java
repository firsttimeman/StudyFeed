package FeedStudy.StudyFeed.global.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushRequest {
    String title, body, link;
}
