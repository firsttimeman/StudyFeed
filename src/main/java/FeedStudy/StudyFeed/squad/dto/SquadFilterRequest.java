package FeedStudy.StudyFeed.squad.dto;


import FeedStudy.StudyFeed.global.type.Topic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SquadFilterRequest {

    private Topic category;
    private String regionMain = "전체";
    private String regionSub = "전체";
    private boolean recruitingOnly = false;

}
