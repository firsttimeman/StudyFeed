package FeedStudy.StudyFeed.openchat.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.openchat.type.ChatRoomUserStatus;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_room_user")
public class ChatRoomUser  extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private boolean isAdmin;

    @Enumerated(EnumType.STRING)
    private ChatRoomUserStatus status;

    public static ChatRoomUser create(ChatRoom room, User user, boolean isAdmin) {
        return ChatRoomUser.builder()
                .chatRoom(room)
                .user(user)
                .isAdmin(isAdmin)
                .status(ChatRoomUserStatus.JOINED)
                .build();
    }
}
