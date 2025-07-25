package FeedStudy.StudyFeed.block.controller;

import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.block.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "유저 차단", description = "특정 유저를 차단합니다.")
    @PostMapping("/{otherId}")
    public ResponseEntity<String> blockUser(@AuthenticationPrincipal User user,
                                            @Parameter(description = "차단할 유저의 ID") @PathVariable User otherId) {
        blockService.createBlock(user, otherId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "유저 차단 해제", description = "특정 유저의 차단을 해제합니다.")
    @DeleteMapping("/{otherId}")
    public ResponseEntity<String> unblockUser(@AuthenticationPrincipal User user,
                                              @Parameter(description = "차단 해제할 유저의 ID") @PathVariable User otherId) {
        blockService.removeBlock(user, otherId);
        return ResponseEntity.ok("사용자 차단을 해제했습니다.");
    }

    @Operation(summary = "차단 목록 조회", description = "자신이 차단한 유저 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<BlockSimpleDto>> getBlocks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(blockService.blockList(user));
    }


}
