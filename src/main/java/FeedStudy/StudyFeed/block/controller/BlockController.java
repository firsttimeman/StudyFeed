package FeedStudy.StudyFeed.block.controller;

import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.block.service.BlockService;
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

    @PostMapping("/{otherId}")
    public ResponseEntity<String> blockUser(@AuthenticationPrincipal User user, @PathVariable User otherId) {
        blockService.createBlock(user, otherId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{otherId}")
    public ResponseEntity<String> unblockUser(@AuthenticationPrincipal User user, @PathVariable User otherId) {
        blockService.removeBlock(user, otherId);
        return ResponseEntity.ok("사용자 차단을 해제했습니다.");
    }

    @GetMapping
    public ResponseEntity<List<BlockSimpleDto>> getBlocks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(blockService.blockList(user));
    }


}
