package FeedStudy.StudyFeed.user.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.global.utils.Utils;
import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Column(nullable = false)
    private String password;

    private String providerType;

    private String providerId;

    private String telecom;



    @Column(nullable = true)
    private String nickName;

    @Column(nullable = false)
    private LocalDate birthDate;


    @Column(nullable = true)
    private Boolean receiveEvent = false;
    @Column(nullable = true)
    private Boolean feedAlarm = false;
    @Column(nullable = true)
    private Boolean feedLikeAlarm = false;
    @Column(nullable = true)
    private Boolean squadChatAlarm = false;
    @Column(nullable = true)
    private Boolean chatroomAlarm = false;
    @Column(nullable = true)
    private Boolean squadNotifyAlarm = false;


    private int reportCount = 0;

    @Column(nullable = false)
    private String gender;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadMember> squads = new ArrayList<>();

    @Column(nullable = true)
    private String fcmToken;

    @Column(nullable = true)
    private String description;


    public void increaseReportCount() {
        this.reportCount++;
    }


    public int getAge() {
        return Utils.calculateAge(birthDate);
    }



//    public List<SimpleGrantedAuthority> getAuthorities() {
//        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.userRole.name()));
//    }

    public User(SignUpRequestDto req, String password, String imageUrl) {
        this.email = req.getEmail();
        this.password = password;
        this.providerType = req.getProviderType();
        this.providerId = req.getProviderId();
        this.imageUrl = imageUrl;
        this.telecom = req.getTelecom();
        this.nickName = "";
        this.birthDate = LocalDate.parse(req.getBirthDate().toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.gender = req.getGender();
        this.userRole = UserRole.USER;
        this.receiveEvent = req.getReceiveEvent().equals("Y");
        this.description = "";
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.userRole.name()));
    }
}
