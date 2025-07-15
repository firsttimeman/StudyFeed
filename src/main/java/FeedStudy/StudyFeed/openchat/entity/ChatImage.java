package FeedStudy.StudyFeed.openchat.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatImage extends BaseEntity {

    private String uniqueName;

    private String originalName;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatMessage chatMessage;

    public ChatImage(String uniqueName, String originalName, String url) {
        this.uniqueName = uniqueName;
        this.originalName = originalName;
        this.url = url;
    }

    public void initChatMessage(ChatMessage message) {
        if(this.chatMessage == null) {
            this.chatMessage = chatMessage;
        }
    }
}
