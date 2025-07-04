package FeedStudy.StudyFeed.notice.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Notice extends BaseEntity {

    @Column(nullable = false)
    private String title;


    @Column(columnDefinition = "TEXT")
    private String content;


}
