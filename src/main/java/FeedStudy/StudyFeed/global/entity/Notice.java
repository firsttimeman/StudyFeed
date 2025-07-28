package FeedStudy.StudyFeed.global.entity;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Notice extends BaseEntity {

    private String title;

    private String content;

    private boolean isVisible;

    private LocalDate publishDate;

}
