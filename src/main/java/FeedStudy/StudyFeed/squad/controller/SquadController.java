package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.squad.dto.*;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.service.SquadService;
import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/squad")
@RequiredArgsConstructor
public class SquadController {

    private final SquadService squadService;

    /** 스쿼드 생성 */
    @Operation(summary = "스쿼드 생성")
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SquadCreateResponse> createSquad(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody SquadRequest requestDto) {
        return ResponseEntity.ok(squadService.createSquad(requestDto, user));
    }

    /** 스쿼드 수정 */
    @Operation(summary = "스쿼드 수정")
    @PutMapping("/modify/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SquadDto> updateSquad(@PathVariable Long id,
                                                @AuthenticationPrincipal User user,
                                                @Valid @RequestBody UpdateSquadRequest req) {
        Squad squad = squadService.updateSquad(id, user, req);
        return ResponseEntity.ok(SquadDto.from(squad));
    }

    /** 스쿼드 삭제 */
    @Operation(summary = "스쿼드 삭제")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteSquad(@PathVariable Long id,
                                              @AuthenticationPrincipal User user,
                                              @RequestParam(defaultValue = "false") boolean isForcedDelete) {
        squadService.deleteSquad(id, user, isForcedDelete);
        return ResponseEntity.ok("Squad deleted");
    }

    /** 내 스쿼드 목록 */
    @Operation(summary = "내 스쿼드 목록")
    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DataResponse> mySquads(@AuthenticationPrincipal User user,
                                                 @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10)
                                                 Pageable pageable) {
        return ResponseEntity.ok(squadService.mySquad(user, pageable));
    }

    /** 홈 스쿼드 목록 (필터) */
    @Operation(summary = "스쿼드 홈 목록")
    @GetMapping("/home")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DataResponse> homeSquads(@ModelAttribute SquadFilterRequest req,
                                                   @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10)
                                                   Pageable pageable) {
        return ResponseEntity.ok(squadService.homeSquad(pageable, req));
    }

    /** 스쿼드 상세 */
    @Operation(summary = "스쿼드 상세")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SquadDetailDto> detail(@AuthenticationPrincipal User user,
                                                 @PathVariable Long id) {
        return ResponseEntity.ok(squadService.detail(user, id));
    }

    /** 스쿼드 모집 마감 */
    @Operation(summary = "스쿼드 모집 마감")
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> closeSquad(@AuthenticationPrincipal User user,
                                           @PathVariable Long id) {
        squadService.closeSquad(user, id);
        return ResponseEntity.ok().build();
    }

    /** 스쿼드 참가 (또는 상태 안내) */
    @Operation(summary = "스쿼드 참가")
    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> join(@AuthenticationPrincipal User user,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(squadService.joinSquad(user, id));
    }

    /** 대기자 목록(오너 전용) */
    @Operation(summary = "대기자 목록 조회(오너 전용)")
    @GetMapping("/{id}/pending")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserSimpleDto>> pendingApplicants(@AuthenticationPrincipal User owner,
                                                                 @PathVariable Long id) {
        return ResponseEntity.ok(squadService.getPendingApplicants(owner, id));
    }

    /** 참가 승인(오너 전용) */
    @Operation(summary = "스쿼드 참가 승인")
    @PutMapping("/{squadId}/approve/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> approve(@AuthenticationPrincipal User owner,
                                        @PathVariable Long squadId,
                                        @PathVariable Long userId) {
        squadService.approveParticipant(owner, userId, squadId);
        return ResponseEntity.ok().build();
    }

    /** 참가 거절(오너 전용) */
    @Operation(summary = "스쿼드 참가 거절")
    @PutMapping("/{squadId}/reject/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> reject(@AuthenticationPrincipal User owner,
                                       @PathVariable Long squadId,
                                       @PathVariable Long userId) {
        squadService.rejectParticipant(owner, userId, squadId);
        return ResponseEntity.ok().build();
    }

    /** 강퇴(오너 전용) */
    @Operation(summary = "스쿼드 멤버 강퇴")
    @PutMapping("/{squadId}/kick/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> kick(@AuthenticationPrincipal User owner,
                                     @PathVariable Long squadId,
                                     @PathVariable Long userId) {
        squadService.kickOffParticipant(owner, userId, squadId);
        return ResponseEntity.ok().build();
    }

    /** 자발적 탈퇴 */
    @Operation(summary = "스쿼드 탈퇴")
    @PutMapping("/{squadId}/leave")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> leave(@AuthenticationPrincipal User user,
                                      @PathVariable Long squadId) {
        squadService.leaveSquad(user, squadId);
        return ResponseEntity.ok().build();
    }
}