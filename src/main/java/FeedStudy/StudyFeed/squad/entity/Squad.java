package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.type.*;
import FeedStudy.StudyFeed.squad.dto.SquadRequest;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Squad extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;


    private String title, category, regionMain, regionSub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender genderRequirement;

    @Column(nullable = false)
    private boolean timeSpecified;

    private int maxParticipants, minAge, maxAge;

    private LocalDate date;

    private LocalTime time;

    private String notice = null; // todo check notice

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinType joinType = JoinType.APPROVAL;

    @Lob
    private String description;

    @Column(nullable = true)
    private int currentCount = 1;

    @Column(nullable = true)
    private int reportCount = 0;


    @OneToMany(mappedBy = "squad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadMember> members = new ArrayList<>();


    @Column(nullable = false)
    private boolean closed = false;

    @Column
    private boolean notifySent = false;


    @Builder
    private Squad(User user, SquadRequest req) {
        this.user = user;
        this.category = req.getCategory();
        this.title = req.getTitle();
        this.description = req.getDescription();
        this.regionMain = req.getRegionMain();
        this.regionSub = req.getRegionSub();
        this.timeSpecified = (req.getTimeSpecified() != null) ? req.getTimeSpecified() : false;
        if(Boolean.TRUE.equals(req.getTimeSpecified())){
            this.time = req.getTime();
        }
        this.genderRequirement = req.getGenderRequirement();
        this.joinType = Optional.ofNullable(req.getJoinType()).orElse(JoinType.APPROVAL);
        this.maxParticipants = req.getMaxParticipants();
        this.minAge = req.getMinAge();
        this.maxAge = req.getMaxAge();
        this.date = req.getDate();
    }

    public static Squad create(User user, SquadRequest req) {
        return new Squad(user, req);
    }

    public void update(SquadRequest req) {


        System.out.println("===== Squad 업데이트 시작 =====");
        System.out.println("기존 title: " + this.title + " → 변경: " + req.getTitle());
        System.out.println("기존 category: " + this.category + " → 변경: " + req.getCategory());
        System.out.println("기존 regionMain: " + this.regionMain + " → 변경: " + req.getRegionMain());
        System.out.println("기존 regionSub: " + this.regionSub + " → 변경: " + req.getRegionSub());
        System.out.println("기존 timeSpecified: " + this.timeSpecified + " → 변경: " + req.getTimeSpecified());
        System.out.println("기존 date: " + this.date + " → 변경: " + req.getDate());
        System.out.println("기존 genderRequirement: " + this.genderRequirement + " → 변경: " + req.getGenderRequirement());
        System.out.println("minAge: " + this.minAge + "변경 -> : " + req.getMinAge());
        System.out.println("maxAge: " + this.maxAge + "변경 -> : " + req.getMaxAge());
        System.out.println("time: " + this.time + "변경 -> : " + req.getTime());
        System.out.println("jointype: " + this.joinType + "변경 -> : " + req.getJoinType());
        System.out.println("maxParticipants: " + this.maxParticipants + "변경 -> : " + req.getMaxParticipants());

        this.category = req.getCategory();
        this.title = req.getTitle();
        this.description = req.getDescription();
        this.regionMain = req.getRegionMain();
        this.regionSub = req.getRegionSub();
        this.timeSpecified = (req.getTimeSpecified() != null) ? req.getTimeSpecified() : false;
        if(Boolean.TRUE.equals(req.getTimeSpecified())){
            this.time = req.getTime();
        }
        this.genderRequirement = req.getGenderRequirement();
        this.joinType = Optional.ofNullable(req.getJoinType()).orElse(JoinType.APPROVAL);
        this.maxParticipants = req.getMaxParticipants();
        this.minAge = req.getMinAge();
        this.maxAge = req.getMaxAge();
        this.date = req.getDate();


        System.out.println("===== Squad 업데이트 완료 =====");
    }



    @PrePersist
    public void prePersist() {
        if(this.genderRequirement == null) {
            this.genderRequirement = Gender.ALL;
        }
    }



    public void increaseCurrentCount() {
        this.currentCount++;
    }

    public void decreaseCurrentCount() {
        this.currentCount--;
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    public void decreaseReportCount() {
        this.reportCount--;
    }

    public boolean isOnlyOneLeft() {
        return members.stream().filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED)
                .count() == 1;
    }

    public void joinParticipant(SquadMember member) {
        this.members.add(member);
        if(this.joinType == JoinType.DIRECT) {
            this.increaseCurrentCount();
        }
    }
}
