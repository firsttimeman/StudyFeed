package FeedStudy.StudyFeed.entity.Squad;

import FeedStudy.StudyFeed.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.dto.SquadUpdateRequestDto;
import FeedStudy.StudyFeed.entity.BaseEntity;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.type.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Squad extends BaseEntity {


    @Column(nullable = false, unique = true)
    private String squadName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitStatus recruitStatus;

    @Column(nullable = false)
    private int peopleNum;

    //todo 추가된 항목
    @Column(nullable = false)
    private int currentPeopleNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SquadGender squadGender;

    @OneToMany(mappedBy = "squad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquadMember> members;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SquadAccessType squadAccessType;

    @Column(nullable = false)
    private String regionMain;

    @Column(nullable = true)
    private String regionSub;

    @Column(nullable = false)
    private LocalDate meetDate;

    @Column(nullable = false)
    private LocalTime meetTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Age age;

    @Column(nullable = true)
    private Integer minAge; // 직접 입력한 연령대

    @Column(nullable = true)
    private Integer maxAge;



    public void update(SquadUpdateRequestDto requestDto) {
        Squad.builder()
                .squadName(requestDto.getSquadName())
                .topic(requestDto.getTopic())
                .peopleNum(requestDto.getPeopleNum())
                .squadGender(requestDto.getSquadGender())
                .meetDate(requestDto.getMeetDate())
                .meetTime(requestDto.getMeetTime())
                .minAge(requestDto.getMinAge())
                .maxAge(requestDto.getMaxAge())
                .squadAccessType(requestDto.getSquadAccessType())
                .regionMain(requestDto.getRegionMain())
                .regionSub(requestDto.getRegionSub())
                .description(requestDto.getDescription())
                .build();
    }

    public static Squad build(SquadCreateRequestDto requestDto, User leader) {
        return Squad.builder()
                .squadName(requestDto.getSquadName())
                .leader(leader)
                .recruitStatus(RecruitStatus.OPEN)
                .topic(requestDto.getTopic())
                .maxAge(requestDto.getMaxAge())
                .minAge(requestDto.getMinAge())
                .squadAccessType(requestDto.getSquadAccessType())
                .meetDate(requestDto.getMeetDate())
                .meetTime(requestDto.getMeetTime())
                .description(requestDto.getDescription())
                .currentPeopleNum(1)
                .peopleNum(requestDto.getPeopleNum())
                .regionMain(requestDto.getRegionMain())
                .regionSub(requestDto.getRegionSub())
                .build();
    }

    public void decreasingCurrentPeopleNum() {
        if (currentPeopleNum > 0) {
            currentPeopleNum--;
        }
    }

}
