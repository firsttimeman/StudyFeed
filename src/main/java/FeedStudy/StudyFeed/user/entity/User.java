package FeedStudy.StudyFeed.user.entity;

import FeedStudy.StudyFeed.auth.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.global.utils.Utils;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nickName;

    @Column(nullable = false)
    private LocalDate birthDate;

    private Boolean receiveEvent = false;

    private int reportCount = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadMember> squads = new ArrayList<>();

    private String description;

    @Builder
    public User(String email, String password, String nickName, LocalDate birthDate,
                Boolean receiveEvent, int reportCount, Gender gender,
                String imageUrl, UserRole userRole, String description) {
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.receiveEvent = receiveEvent;
        this.reportCount = reportCount;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.userRole = userRole;
        this.description = description;
    }

    public User(SignUpRequestDto req, String password, String imageUrl) {
        this.email = req.getEmail();
        this.password = password;
        this.imageUrl = imageUrl;
        this.nickName = "";
        this.birthDate = req.getBirth();
        this.gender = req.getGender();
        this.userRole = UserRole.USER;
        this.receiveEvent = "Y".equals(req.getReceiveEvent());
        this.description = "";
    }

    public int getAge() {
        return Utils.calculateAge(birthDate);
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickName(String nickName) {
        this.nickName = nickName;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}