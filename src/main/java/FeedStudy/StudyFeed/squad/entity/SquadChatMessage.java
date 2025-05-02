package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.squad.dto.SquadMessageRequest;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SquadChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    @OneToOne
    private User sender;

    @Column(columnDefinition = "text")
    private String content;

//
//    public SquadChatMessage(Squad squad, User user, String content) {
//        this.squad = squad;
//        this.sender = user;
//        this.content = content;
//    }
}
