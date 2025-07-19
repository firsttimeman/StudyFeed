package FeedStudy.StudyFeed.global.controller;

import FeedStudy.StudyFeed.global.dto.SettingUpdateRequest;
import FeedStudy.StudyFeed.global.entity.Setting;
import FeedStudy.StudyFeed.global.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setting")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    public ResponseEntity<?> getSetting() {
        Setting setting = settingService.getSetting();
        return ResponseEntity.ok(setting);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSetting(@RequestBody SettingUpdateRequest req) {
        Setting setting = settingService.updateSetting(req.getPrivacy(), req.getTerms(), req.isUnderMaintenance());
        return ResponseEntity.ok(setting);
    }
}
