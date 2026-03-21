package FeedStudy.StudyFeed.notice.domain;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
public class Notice extends BaseEntity {

    private String title;

    private String content;

    private boolean visible;

    private LocalDate publishDate;

    @Builder
    public Notice(String title, String content, boolean visible, LocalDate publishDate) {
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.publishDate = publishDate;
    }

    public void update(String title, String content, boolean visible, LocalDate publishDate) {
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.publishDate = publishDate;
    }
}