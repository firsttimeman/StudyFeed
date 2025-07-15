package FeedStudy.StudyFeed.report.controller;

import FeedStudy.StudyFeed.report.dto.ReportRequest;
import FeedStudy.StudyFeed.report.service.ReportService;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportUser(@AuthenticationPrincipal User user, @RequestParam Long reportedId,
                                        @RequestBody ReportRequest req) {
        reportService.reportUser(user.getId(), reportedId, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reportSquad")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportSquad(@AuthenticationPrincipal User user, @RequestParam Long squadId,
                                         @RequestBody ReportRequest req) {
        reportService.reportSquad(user.getId(), squadId, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reportFeed")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportFeed(@AuthenticationPrincipal User user, @RequestParam Long feedId,
                                        @RequestBody ReportRequest req) {
        reportService.reportFeed(user.getId(), feedId, req);
        return ResponseEntity.ok().build();
    }
}
