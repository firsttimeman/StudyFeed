package FeedStudy.StudyFeed.user.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.Telecom;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.global.utils.Utils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collections;
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


    @Column(unique = true, nullable = true)
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

    private String providerType;
    private String providerId;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean receiveEventAlarm, receiveFeedAlarm;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadMember> squads;

    @Column(nullable = false)
    private LocalDate birthDate;

    public int getAge() {
        return Utils.calculateAge(birthDate);
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.userRole.name()));
    }
}
