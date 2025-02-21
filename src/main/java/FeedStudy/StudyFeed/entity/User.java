package FeedStudy.StudyFeed.entity;

import FeedStudy.StudyFeed.type.Gender;
import FeedStudy.StudyFeed.type.Telecom;
import FeedStudy.StudyFeed.type.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import lombok.*;

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


    //nickname TODO 추가하기
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


}
