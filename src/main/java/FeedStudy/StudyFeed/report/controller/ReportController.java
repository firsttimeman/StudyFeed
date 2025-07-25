package FeedStudy.StudyFeed.report.controller;

import FeedStudy.StudyFeed.report.dto.ReportRequest;
import FeedStudy.StudyFeed.report.service.ReportService;
import FeedStudy.StudyFeed.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "사용자 신고", description = "특정 사용자를 신고합니다. 신고 사유와 세부 내용을 포함해야 합니다.")
    @PostMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportUser(  @Parameter(hidden = true) @AuthenticationPrincipal User user,
                                          @Parameter(description = "신고 대상 사용자 ID") @RequestParam Long reportedId,
                                          @Valid @RequestBody ReportRequest req) {
        reportService.reportUser(user.getId(), reportedId, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스쿼드 신고", description = "특정 스쿼드를 신고합니다. 신고 사유와 세부 내용을 포함해야 합니다.")
    @PostMapping("/reportSquad")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportSquad( @Parameter(hidden = true) @AuthenticationPrincipal User user,
                                          @Parameter(description = "신고 대상 스쿼드 ID") @RequestParam Long squadId,
                                          @Valid @RequestBody ReportRequest req) {
        reportService.reportSquad(user.getId(), squadId, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "피드 신고", description = "특정 피드를 신고합니다. 신고 사유와 세부 내용을 포함해야 합니다.")
    @PostMapping("/reportFeed")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportFeed( @Parameter(hidden = true) @AuthenticationPrincipal User user,
                                         @Parameter(description = "신고 대상 피드 ID") @RequestParam Long feedId,
                                         @Valid @RequestBody ReportRequest req) {
        reportService.reportFeed(user.getId(), feedId, req);
        return ResponseEntity.ok().build();
    }
}
