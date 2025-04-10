package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SquadAccessType {
    OPEN("누구나 참여 가능"),
    APPROVAL("승인을 받아야 가능");

    private final String description;
}
