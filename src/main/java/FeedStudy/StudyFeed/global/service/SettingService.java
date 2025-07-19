package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.global.entity.Setting;
import FeedStudy.StudyFeed.global.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;

    public Setting getSetting() {
        return settingRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("셋팅을 찾지 못했습니다."));
    }

    public Setting updateSetting(String privacy, String terms, boolean isUnderMaintenance) {
        Setting setting = getSetting();
        setting.setPrivacy(privacy);
        setting.setTerms(terms);
        setting.setUnderMaintenance(isUnderMaintenance);
        return settingRepository.save(setting);
    }
}
