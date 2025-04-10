package FeedStudy.StudyFeed.block.service;

import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.user.entity.User;

import java.util.List;

public interface BlockServiceImpl {

    boolean updateBlock(User blocker, Long BlockedId); // todo 뻬야할까?

    void createBlock(User blocker, Long BlockedId);

    void removeBlock(User blocker, Long BlockedId);

    List<BlockSimpleDto> blockList(User user);


}
