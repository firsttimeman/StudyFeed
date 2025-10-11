package FeedStudy.StudyFeed.squad.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "squad_chat")
public class SquadChat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    @Column(columnDefinition = "text")
    private String message;

    private String notice = null;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    private boolean deletable;

    @OneToMany(mappedBy = "squadChat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadChatImage> images = new ArrayList<>();




    public static SquadChat text(User user, Squad squad, String message) {
        return SquadChat.builder()
                .user(user)
                .squad(squad)
                .message(message)
                .type(ChatType.TEXT)
                .deletable(true)
                .build();
    }

    public static SquadChat image(User user, Squad squad, List<SquadChatImage> images) {
        SquadChat squadChat = SquadChat.builder()
                .user(user)
                .squad(squad)
                .message("") // 이미지 전용 메시지는 비워둠
                .type(ChatType.IMAGE)
                .deletable(true)
                .build();
        squadChat.addImages(images);
        return squadChat;
    }

    public static SquadChat notice(User user, Squad squad, String noticeMessage) {
        return SquadChat.builder()
                .user(user)
                .squad(squad)
                .notice(noticeMessage)
                .deletable(false)
                .build();
    }

    public void addImages(List<SquadChatImage> imageList) {
        for (SquadChatImage image : imageList) {
            image.initSquadChat(this);
            this.images.add(image);
        }
    }


    public static SquadChat date(Squad squad, LocalDate date) {
        String[] days = { "월", "화", "수", "목", "금", "토", "일" };
        String format = String.format("%d. %02d. %02d (%s)",
                date.getYear(), date.getMonthValue(), date.getDayOfMonth(), days[date.getDayOfWeek().getValue() - 1]);

        return SquadChat.builder()
                .squad(squad)
                .message(format)
                .type(ChatType.DATE)
                .deletable(false)
                .build();

    }



    public void delete(){
        this.message = "삭제된 메세지 입니다.";
        this.deletable = false;
        this.type = ChatType.TEXT;
    }
}
