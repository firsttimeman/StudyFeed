package FeedStudy.StudyFeed.openchat.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    private String title;

    @Enumerated(EnumType.STRING)
    private Topic topic;

    @Column(length = 100)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    private int participantCount;

    private int maxParticipants;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomUser> users = new ArrayList<>();

    public void incrementParticipantCount() {
        if (this.participantCount >= this.maxParticipants) {
            throw new IllegalStateException("최대 참여 인원을 초과했습니다.");
        }
        this.participantCount++;
    }

    public void decrementParticipantCount() {
        this.participantCount = Math.max(0, this.participantCount - 1);
    }

    public static ChatRoom create(User owner, String title, Topic topic, String description, int maxParticipants) {
        return ChatRoom.builder()
                .owner(owner)
                .title(title)
                .topic(topic)
                .description(description)
                .participantCount(1)
                .maxParticipants(maxParticipants)
                .build();
    }

}
