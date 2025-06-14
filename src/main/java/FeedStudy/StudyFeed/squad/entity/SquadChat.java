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
public class SquadChat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    private ChatType type = ChatType.TEXT;

    @Column(columnDefinition = "text")
    private String message;

    private int imageCount = 0;

    private boolean deletable = true;

    @OneToMany(mappedBy = "squadChat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadChatImage> images = new ArrayList<>();

    public SquadChat(User user, Squad squad, ChatType type, String message, boolean deletable) {
        this.user = user;
        this.squad = squad;
        this.type = type;
        this.message = message;
        this.deletable = deletable;
    }

    public static SquadChat text(User user, Squad squad, String message) {
        return new SquadChat(user, squad, ChatType.TEXT, message, true);
    }

    public static SquadChat image(User user, Squad squad, List<SquadChatImage> images) {
        SquadChat squadChat = new SquadChat(user, squad, ChatType.IMAGE, "", true);
        squadChat.setImageCount(images.size());
        squadChat.addImages(images);
        return squadChat;
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


    public void addImages(List<SquadChatImage> images) {
        images.forEach(image ->  {
            this.images.add(image);
            image.initSquadChat(this);
        });
    }


}
