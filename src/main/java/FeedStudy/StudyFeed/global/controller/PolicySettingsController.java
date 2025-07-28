package FeedStudy.StudyFeed.global.controller;

import FeedStudy.StudyFeed.global.dto.PolicyRequest;
import FeedStudy.StudyFeed.global.entity.PolicySettings;
import FeedStudy.StudyFeed.global.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication") // Swagger에 토큰 인증 명시
@Tag(name = "정책관리", description = "이용약관 및 개인정보처리방침 API")
public class PolicySettingsController {

    private final PolicyService policyService;

    @Operation(summary = "정책 생성", description = "관리자가 이용약관 및 개인정보처리방침을 최초 생성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicySettings> createPolicy(@RequestBody PolicyRequest request) {
        return ResponseEntity.ok(policyService.createPolicy(request.getPrivacy(), request.getTerms()));
    }

    @Operation(summary = "개인정보처리방침 조회", description = "사용자에게 공개된 개인정보처리방침을 조회합니다.")
    @GetMapping("/privacy")
    public ResponseEntity<String> getPrivacy() {
        return ResponseEntity.ok(policyService.getPolicy().getPrivacy());
    }

    @Operation(summary = "이용약관 조회", description = "사용자에게 공개된 이용약관을 조회합니다.")
    @GetMapping("/terms")
    public ResponseEntity<String> getTerms() {
        return ResponseEntity.ok(policyService.getPolicy().getTerms());
    }

    @Operation(summary = "정책 수정", description = "관리자가 이용약관 또는 개인정보처리방침을 수정합니다.")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicySettings> updatePolicy(@RequestBody PolicyRequest request) {
        return ResponseEntity.ok(policyService.updatePolicy(request.getPrivacy(), request.getTerms()));
    }

    @Operation(summary = "정책 삭제", description = "관리자가 정책을 삭제합니다.")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePolicy() {
        policyService.deletePolicy();
        return ResponseEntity.noContent().build();
    }

}
