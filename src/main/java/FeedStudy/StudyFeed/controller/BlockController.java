package FeedStudy.StudyFeed.controller;

import FeedStudy.StudyFeed.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{id}")
    public ResponseEntity<String> blockUser(@AuthenticationPrincipal User user, @PathVariable Long id) {
        blockService.createBlock(user, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> unblockUser(@AuthenticationPrincipal User user, @PathVariable Long id) {
        blockService.removeBlock(user, id);
        return ResponseEntity.ok("사용자 차단을 해제했습니다.");
    }

    @GetMapping
    public ResponseEntity<List<BlockSimpleDto>> getBlocks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(blockService.blockList(user));
    }


}
