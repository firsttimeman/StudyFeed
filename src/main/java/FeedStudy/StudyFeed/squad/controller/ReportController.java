package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.squad.dto.ReportRequest;
import FeedStudy.StudyFeed.squad.service.ReportService;
import jakarta.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reportUser")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportUser(@RequestParam Long reporterId, @RequestParam Long reportedId,
                                        @RequestBody ReportRequest req) {
        reportService.reportUser(reporterId, reportedId, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reportSquad")
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> reportSquad(@RequestParam Long reporterId, @RequestParam Long squadId,
                                         @RequestBody ReportRequest req) {
        reportService.reportSquad(reporterId, squadId, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reportFeed")
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> reportFeed(@RequestParam Long reporterId, @RequestParam Long feedId,
                                        @RequestBody ReportRequest req) {
        reportService.reportFeed(reporterId, feedId, req);
        return ResponseEntity.ok().build();
    }
}
