package FeedStudy.StudyFeed.entity;

import FeedStudy.StudyFeed.entity.Squad.SquadMember;
import FeedStudy.StudyFeed.type.Gender;
import FeedStudy.StudyFeed.type.Telecom;
import FeedStudy.StudyFeed.type.UserRole;
import FeedStudy.StudyFeed.utils.Utils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {

    @Email
    @Column(unique = true, nullable = false)
    private String email;


    @Column(unique = true, nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Telecom telecom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column(nullable = true)
    private String fcmToken;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean receiveEventAlarm, receiveFeedAlarm;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadMember> squads;

    @Column(nullable = false)
    private LocalDate birthDate;

    public int getAge() {
        return Utils.calculateAge(birthDate);
    }
}
