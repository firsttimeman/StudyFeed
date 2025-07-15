package FeedStudy.StudyFeed.openchat.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.ChatType;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    @Column(length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatType type = ChatType.TEXT;

    private int imageCount = 0;

    private boolean deletable = true;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatImage> images = new ArrayList<>();

    public static ChatMessage createText(User sender, ChatRoom room, String content) {
        return ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .type(ChatType.TEXT)
                .build();
    }

    public static ChatMessage notice(User sender, ChatRoom room, String content) {
        return ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .type(ChatType.NOTICE)
                .deletable(false)
                .build();
    }

    public static ChatMessage image(User sender, ChatRoom room, List<ChatImage> images) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content("")
                .type(ChatType.IMAGE)
                .deletable(true)
                .build();

        message.addImage(images);
        return message;
    }

    public static ChatMessage date(ChatRoom room, LocalDate date) {

        String[] days = { "월", "화", "수", "목", "금", "토", "일" };
        String format = String.format("%d. %02d. %02d (%s)",
                date.getYear(), date.getMonthValue(), date.getDayOfMonth(), days[date.getDayOfWeek().getValue() - 1]);

        return ChatMessage.builder()
                .chatRoom(room)
                .content(format)
                .type(ChatType.DATE)
                .deletable(false)
                .build();
    }


    public void softDelete() {
        this.content = "삭제된 메시지입니다.";
        this.deletable = false;
        this.type = ChatType.TEXT;
    }

    public void addImage(List<ChatImage> images) {
        for (ChatImage image : images) {
            image.initChatMessage(this);
            this.images.add(image);
        }
    }

}
