package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "squad_chat_image")
public class SquadChatImage extends BaseEntity {

    private String uniqueName; // UUID.jpg
    private String originName; // abc.jpg
    private String url;        // https://s3.aws.com/abc/cat.png

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_chat_id")
    private SquadChat squadChat;

    public SquadChatImage(String uniqueName, String originName, String url) {
        this.uniqueName = uniqueName;
        this.originName = originName;
        this.url = url;
    }

    public void initSquadChat(SquadChat squadChat) {
        if (this.squadChat == null) {
            this.squadChat = squadChat;
        }
    }
}