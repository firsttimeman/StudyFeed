package FeedStudy.StudyFeed.squad.controller;

import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.squad.dto.*;
import FeedStudy.StudyFeed.squad.service.SquadService;
import FeedStudy.StudyFeed.user.entity.User;
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
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createSquad(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody SquadRequest requestDto) {

        squadService.createSquad(requestDto, user);
        return ResponseEntity.ok("Squad created");
    }



    @PutMapping("/modify/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updateSquad(@PathVariable Long id, @AuthenticationPrincipal User user,
                                              @RequestBody SquadRequest req) {
        squadService.updateSquad(id, user, req);
        return ResponseEntity.ok("Squad updated");
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteSquad(@PathVariable Long id,
                                              @AuthenticationPrincipal User user,
                                              @RequestParam(defaultValue = "false") boolean isForcedDelete) {
        squadService.deleteSquad(id, user, isForcedDelete);
        return ResponseEntity.ok("Squad deleted");
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> mySquads(@AuthenticationPrincipal User user,
                                       @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10)
                                       Pageable pageable) {
        DataResponse dataResponse = squadService.mySquad(user, pageable);
        return ResponseEntity.ok(dataResponse.toString());
    }

    @GetMapping("/home")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> homeSquads(@AuthenticationPrincipal User user, @ModelAttribute SquadFilterRequest req,
                                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {
        return ResponseEntity.ok(squadService.homeSquad(user, pageable, req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> detail(@AuthenticationPrincipal User user, @PathVariable Long id) {
        SquadDetailDto detail = squadService.detail(user, id);
        return ResponseEntity.ok(detail);
    }


    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeSquad(@AuthenticationPrincipal User user, @PathVariable Long id) {
        squadService.closeSquad(user, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{squadId}/approve/{userId}")
    public ResponseEntity<?> approveMember(@AuthenticationPrincipal User user, @PathVariable Long squadId,
                                                @PathVariable("userId") Long userId) {
        squadService.approveParticipant(user, userId, squadId);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{squadId}/reject/{userId}")
    public ResponseEntity<?> rejectParticipant(@AuthenticationPrincipal User user, @PathVariable Long squadId,
                                               @PathVariable Long userId) {
        squadService.rejectParticipant(user, userId, squadId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{squadId}/kick/{userId}")
    public ResponseEntity<?> kickOffParticipant(@AuthenticationPrincipal User user, @PathVariable Long squadId,
                                                @PathVariable Long userId) {
        squadService.kickOffParticipant(user, userId, squadId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{squadId}/leave")
    public ResponseEntity<?> leaveSquad(@AuthenticationPrincipal User user, @PathVariable Long squadId) {
        squadService.kickOffParticipant(user, squadId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/join-or-token")
    public ResponseEntity<?> joinOrGetToken(@AuthenticationPrincipal User user, @PathVariable Long id) {
        Map<String, String> result = squadService.joinOrGetChatToken(user, id);
        return ResponseEntity.ok(result);
    }




    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getParticipants(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(squadService.getParticipants(user, id));
    }






}
