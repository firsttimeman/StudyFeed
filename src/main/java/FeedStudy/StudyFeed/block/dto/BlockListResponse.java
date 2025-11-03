package FeedStudy.StudyFeed.block.dto;

import java.util.List;

public record BlockListResponse(
        int count,
        List<BlockSimpleDto> blocks
) {
    public static BlockListResponse from(List<BlockSimpleDto> blocks) {
        return new BlockListResponse(blocks.size(), blocks);
    }
}
