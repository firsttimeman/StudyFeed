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

    @Column(nullable = false)
    private LocalDate meetDate;

    @Column(nullable = false)
    private LocalTime meetTime;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SquadAccessType squadAccessType = SquadAccessType.APPROVAL;

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
            this.meetTime = req.getMeetTime();
        }
        this.genderRequirement = req.getGenderRequirement();
        this.squadAccessType = Optional.ofNullable(req.getSquadAccessType()).orElse(SquadAccessType.APPROVAL);
        this.maxParticipants = req.getMaxParticipants();
        this.minAge = req.getMinAge();
        this.maxAge = req.getMaxAge();
        this.meetDate = req.getMeetDate();
    }

    public static Squad create(User user, SquadRequest req) {
        return new Squad(user, req);
    }

    public void update(SquadRequest req) {

        this.category = req.getCategory();
        this.title = req.getTitle();
        this.description = req.getDescription();
        this.regionMain = req.getRegionMain();
        this.regionSub = req.getRegionSub();
        this.timeSpecified = (req.getTimeSpecified() != null) ? req.getTimeSpecified() : false;
        if(Boolean.TRUE.equals(req.getTimeSpecified())){
            this.meetTime = req.getMeetTime();
        }
        this.genderRequirement = req.getGenderRequirement();
        this.squadAccessType = Optional.ofNullable(req.getSquadAccessType()).orElse(SquadAccessType.APPROVAL);
        this.maxParticipants = req.getMaxParticipants();
        this.minAge = req.getMinAge();
        this.maxAge = req.getMaxAge();
        this.meetDate = req.getMeetDate();
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
        if(this.squadAccessType == SquadAccessType.DIRECT) {
            this.increaseCurrentCount();
        }
    }
}
