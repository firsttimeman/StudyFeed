package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.squad.dto.*;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.service.SquadService;
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

import java.util.Map;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/squad")
@RequiredArgsConstructor
public class SquadController {

    private final SquadService squadService;

    /**
     * 스쿼드 등록
     * @param user
     * @param requestDto
     * @return
     */

    @Operation(summary = "스쿼드 생성", description = "스쿼드를 생성하고 채팅 토큰을 반환합니다.")
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createSquad(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody SquadRequest requestDto) {
        System.out.println(requestDto);
        Map<String, String> squadWithToken = squadService.createSquadWithToken(requestDto, user);
        return ResponseEntity.ok(squadWithToken);
    }

    @Operation(summary = "스쿼드 수정", description = "스쿼드 정보를 수정합니다.")
    @PutMapping("/modify/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SquadDto> updateSquad(
            @Parameter(description = "수정할 스쿼드 ID") @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SquadRequest req) {
        Squad squad = squadService.updateSquad(id, user, req);
        return ResponseEntity.ok(SquadDto.from(squad));
    }

    @Operation(summary = "스쿼드 삭제", description = "스쿼드를 삭제합니다. 생성자만 삭제할 수 있으며, 강제 삭제 여부를 설정할 수 있습니다.")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteSquad(
            @Parameter(description = "삭제할 스쿼드 ID") @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Parameter(description = "강제 삭제 여부") @RequestParam(defaultValue = "false") boolean isForcedDelete) {
        squadService.deleteSquad(id, user, isForcedDelete);
        return ResponseEntity.ok("Squad deleted");
    }

    @Operation(summary = "내 스쿼드 목록 조회", description = "로그인한 사용자가 생성 또는 참여 중인 스쿼드 목록을 조회합니다.")
    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DataResponse> mySquads(
            @AuthenticationPrincipal User user,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10)
            Pageable pageable) {
        DataResponse dataResponse = squadService.mySquad(user, pageable);
        return ResponseEntity.ok(dataResponse);
    }

    @Operation(summary = "스쿼드 홈 화면 조회", description = "홈 피드에 표시될 스쿼드 목록을 조회합니다. 필터링 조건과 페이징 지원.")
    @GetMapping("/home")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> homeSquads(
            @AuthenticationPrincipal User user,
            @ModelAttribute SquadFilterRequest req,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10)
            Pageable pageable) {
        return ResponseEntity.ok(squadService.homeSquad(user, pageable, req));
    }

    @Operation(summary = "스쿼드 상세 조회", description = "스쿼드 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> detail(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long id) {
        SquadDetailDto detail = squadService.detail(user, id);
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "스쿼드 모집 마감", description = "스쿼드 모집을 마감합니다.")
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> closeSquad(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long id) {
        squadService.closeSquad(user, id);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "스쿼드 참가 승인", description = "스쿼드 생성자가 특정 유저의 참가를 승인합니다.")
    @PutMapping("/{squadId}/approve/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> approveMember(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long squadId,
            @Parameter(description = "승인할 사용자 ID") @PathVariable Long userId) {
        squadService.approveParticipant(user, userId, squadId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스쿼드 참가 거절", description = "스쿼드 생성자가 특정 유저의 참가를 거절합니다.")
    @PutMapping("/{squadId}/reject/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> rejectParticipant(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long squadId,
            @Parameter(description = "거절할 사용자 ID") @PathVariable Long userId) {
        squadService.rejectParticipant(user, userId, squadId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스쿼드 멤버 강퇴", description = "스쿼드 생성자가 특정 유저를 강퇴합니다.")
    @PutMapping("/{squadId}/kick/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> kickOffParticipant(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long squadId,
            @Parameter(description = "강퇴할 사용자 ID") @PathVariable Long userId) {
        squadService.kickOffParticipant(user, userId, squadId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스쿼드 탈퇴", description = "사용자가 스쿼드에서 스스로 탈퇴합니다.")
    @PutMapping("/{squadId}/leave")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> leaveSquad(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long squadId) {
        squadService.leaveSquad(user, squadId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스쿼드 참가 또는 채팅 토큰 반환", description = "스쿼드에 참가하거나 기존 참가자라면 채팅 토큰을 반환합니다.")
    @PostMapping("/{id}/join-or-token")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> joinOrGetToken(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long id) {
        Map<String, String> result = squadService.joinOrGetChatToken(user, id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "스쿼드 채팅 토큰 재발급", description = "기존 참가자에게 채팅 토큰을 재발급합니다.")
    @GetMapping("/{squadId}/refreshchattoken")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> refreshChatToken(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long squadId) {
        Map<String, String> token = squadService.refreshSquadChatToken(squadId, user);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "스쿼드 참가자 목록 조회", description = "스쿼드에 참가한 유저들의 목록을 반환합니다.")
    @GetMapping("/{id}/participants")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getParticipants(
            @AuthenticationPrincipal User user,
            @Parameter(description = "스쿼드 ID") @PathVariable Long id) {
        return ResponseEntity.ok(squadService.getParticipants(user, id));
    }





}
