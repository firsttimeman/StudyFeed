package FeedStudy.StudyFeed.user.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.global.utils.Utils;
import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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



    @Column(unique = true, nullable = true)
    private String nickName;

    @Column(nullable = false)
    private LocalDate birthDate;


    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean  receiveEvent, feedAlarm, feedLikeAlarm, squadChatAlarm, chatroomAlarm, squadNotifyAlarm;


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
    }
}
