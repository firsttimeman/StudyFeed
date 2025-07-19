package FeedStudy.StudyFeed.global.dto;

import lombok.Getter;

@Getter
public class SettingUpdateRequest {
    private String privacy;
    private String terms;
    private boolean isUnderMaintenance;
}
