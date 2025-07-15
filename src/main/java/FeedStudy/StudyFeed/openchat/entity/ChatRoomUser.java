package FeedStudy.StudyFeed.openchat.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUser  extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private boolean isAdmin;

    public static ChatRoomUser create(ChatRoom room, User user, boolean isAdmin) {
        return ChatRoomUser.builder()
                .chatRoom(room)
                .user(user)
                .isAdmin(isAdmin)
                .build();
    }
}
