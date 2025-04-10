package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Telecom {
    SKT("SKT"), LG("LG"), KT("KT");

    private final String description;
}
