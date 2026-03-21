package FeedStudy.StudyFeed.squad.entity;

import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.squad.dto.SquadRequest;
import FeedStudy.StudyFeed.squad.dto.UpdateSquadRequest;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "squad")
public class Squad extends BaseEntity {

    /** -------------------- 기본 정보 -------------------- **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Topic category;

    @Column(nullable = false, length = 50)
    private String title;

    @Lob
    private String description;

    /** -------------------- 지역 -------------------- **/
    @Column(nullable = false, length = 30)
    private String regionMain;

    @Column(nullable = false, length = 50)
    private String regionSub;

    /** -------------------- 모집 조건 -------------------- **/
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender genderRequirement = Gender.ALL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinType joinType = JoinType.APPROVAL;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private int minAge;

    @Column(nullable = false)
    private int maxAge;

    /** -------------------- 일정 -------------------- **/
    @Column(nullable = false)
    private LocalDate date;

    private LocalTime time;

    @Column(nullable = false)
    private boolean timeSpecified = false;

    /** -------------------- 상태 -------------------- **/
    @Column(nullable = false)
    private boolean closed = false;

    @Column(nullable = false)
    private int currentCount = 1; // 리더 포함

    @Column(nullable = false)
    private int reportCount = 0;

    @Column(length = 500)
    private String notice;

    /** -------------------- 연관관계 -------------------- **/
    @OneToMany(mappedBy = "squad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadMember> members = new ArrayList<>();

    /** -------------------- 생성 -------------------- **/
    @Builder(builderMethodName = "createBuilder")
    private Squad(
            User user,
            Topic category,
            String title,
            String description,
            String regionMain,
            String regionSub,
            Gender genderRequirement,
            JoinType joinType,
            int maxParticipants,
            int minAge,
            int maxAge,
            LocalDate date,
            LocalTime time,
            boolean timeSpecified
    ) {
        this.user = user;
        this.category = Optional.ofNullable(category).orElse(Topic.OTHER);
        this.title = title;
        this.description = description;
        this.regionMain = regionMain;
        this.regionSub = regionSub;
        this.genderRequirement = Optional.ofNullable(genderRequirement).orElse(Gender.ALL);
        this.joinType = Optional.ofNullable(joinType).orElse(JoinType.APPROVAL);
        this.maxParticipants = maxParticipants;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.date = date;
        this.timeSpecified = timeSpecified;
        this.time = timeSpecified ? time : null;
    }

    public static Squad create(User user, SquadRequest req) {
        Squad squad = Squad.createBuilder()
                .user(user)
                .category(req.getCategory())
                .title(req.getTitle())
                .description(req.getDescription())
                .regionMain(req.getRegionMain())
                .regionSub(req.getRegionSub())
                .genderRequirement(req.getGenderRequirement())
                .joinType(Optional.ofNullable(req.getJoinType()).orElse(JoinType.APPROVAL))
                .maxParticipants(req.getMaxParticipants())
                .minAge(req.getMinAge())
                .maxAge(req.getMaxAge())
                .date(req.getDate())
                .time(req.getTime())
                .timeSpecified(Boolean.TRUE.equals(req.getTimeSpecified()))
                .build();

        squad.currentCount = 1;

        if (squad.maxParticipants <= 1) {
            squad.closed = true;
        }

        return squad;
    }

    /** -------------------- 수정 -------------------- **/
    public void updateExceptCapacity(UpdateSquadRequest req) {
        this.category = req.getCategory() != null ? req.getCategory() : this.category;
        this.title = req.getTitle() != null ? req.getTitle() : this.title;
        this.description = req.getDescription() != null ? req.getDescription() : this.description;
        this.regionMain = req.getRegionMain() != null ? req.getRegionMain() : this.regionMain;
        this.regionSub = req.getRegionSub() != null ? req.getRegionSub() : this.regionSub;
        this.genderRequirement = req.getGenderRequirement() != null ? req.getGenderRequirement() : this.genderRequirement;
        this.joinType = req.getJoinType() != null ? req.getJoinType() : this.joinType;
        this.minAge = req.getMinAge() != null ? req.getMinAge() : this.minAge;
        this.maxAge = req.getMaxAge() != null ? req.getMaxAge() : this.maxAge;
        this.date = req.getDate() != null ? req.getDate() : this.date;

        if (req.getTimeSpecified() != null) {
            this.timeSpecified = req.getTimeSpecified();
        }

        if (this.timeSpecified) {
            if (req.getTime() != null) {
                this.time = req.getTime();
            }
        } else {
            this.time = null;
        }
    }

    public void changeMaxParticipants(int newMax) {
        this.maxParticipants = newMax;
    }

    /** -------------------- 상태 제어 -------------------- **/
    public void increaseCurrentCount() {
        this.currentCount++;
    }

    public void decreaseCurrentCount() {
        if (this.currentCount > 0) {
            this.currentCount--;
        }
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    public void decreaseReportCount() {
        if (this.reportCount > 0) {
            this.reportCount--;
        }
    }

    public void close() {
        this.closed = true;
    }

    public void open() {
        this.closed = false;
    }

    public boolean isOnlyOneLeft() {
        return this.currentCount == 1;
    }

    public void joinParticipant(SquadMember member) {
        if (member == null) {
            return;
        }
        this.members.add(member);
    }

    @PrePersist
    public void prePersist() {
        if (this.genderRequirement == null) {
            this.genderRequirement = Gender.ALL;
        }
        if (this.joinType == null) {
            this.joinType = JoinType.APPROVAL;
        }
        if (this.category == null) {
            this.category = Topic.OTHER;
        }
    }
}