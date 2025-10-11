package FeedStudy.StudyFeed.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NickNameCheckResponse {
    private boolean valid;
    private boolean available;
    private String message;
    private String normalized;

}
