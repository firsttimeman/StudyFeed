package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.squad.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.squad.dto.SquadUpdateRequestDto;
import FeedStudy.StudyFeed.squad.service.SquadService;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping
    public ResponseEntity<String> createSquad(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody SquadCreateRequestDto requestDto) {

        squadService.createSquad(requestDto, user);
        return ResponseEntity.ok("Squad created");
    }

    /**
     * 스쿼드 수정
     * @param squadId
     * @param user
     * @param requestDto
     * @return
     */

    @PutMapping("/{squadId")
    public ResponseEntity<String> updateSquad(@PathVariable Long squadId, @AuthenticationPrincipal User user,
                                              @Valid @RequestBody SquadUpdateRequestDto requestDto) {
        squadService.updateSquad(squadId, user, requestDto);
        return ResponseEntity.ok("Squad updated");
    }

    @DeleteMapping("/{squadId")
    public ResponseEntity<String> deleteSquad(@PathVariable Long squadId,
                                              @AuthenticationPrincipal User user) {
        squadService.deleteSquad(squadId, user);
        return ResponseEntity.ok("Squad deleted");
    }

    @PostMapping("/{squadId}/leave")
    public ResponseEntity<String> leaveSquad(@PathVariable Long squadId, @AuthenticationPrincipal User user) {
        squadService.leaveSquad(squadId, user);
        return ResponseEntity.ok("Squad leaved");
    }


    @PostMapping("/member/{memberId}/approve")
    public ResponseEntity<String> approveMember(@PathVariable Long memberId, @AuthenticationPrincipal User user) {
        squadService.approveMember(memberId, user);
        return ResponseEntity.ok("Squad approved");
    }

    @PostMapping("/{squadId}/kick/{targetUserId}")
    public ResponseEntity<String> kickMember(@PathVariable Long squadId,
                                             @AuthenticationPrincipal User leader,
                                             @PathVariable Long targetUserId) {
        squadService.kickMember(squadId, leader, targetUserId);
        return ResponseEntity.ok("회원 강퇴 완료");
    }

    @PostMapping("/{squadId}/close")
    public ResponseEntity<String> closeRecruit(@PathVariable Long squadId,
                                               @AuthenticationPrincipal User user) {
        squadService.closeRecruitment(squadId, user);
        return ResponseEntity.ok("모집 마감 완료");
    }

    @PostMapping("/{squadId}/report/{targetUserId}")
    public ResponseEntity<String> reportMember(@PathVariable Long squadId,
                                               @AuthenticationPrincipal User reporter,
                                               @PathVariable Long targetUserId,
                                               @RequestBody String reason) {
        squadService.reportMember(squadId, reporter, targetUserId, reason);
        return ResponseEntity.ok("신고 완료");
    }


}
