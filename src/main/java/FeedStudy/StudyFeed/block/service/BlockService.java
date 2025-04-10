package FeedStudy.StudyFeed.block.service;

import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockService extends AbstractBlockService {

    public BlockService(BlockRepository blockRepository, UserRepository userRepository) {
        super(blockRepository, userRepository);
    }


    @Transactional
    public boolean updateBlock(User blocker, Long blockedId) {

        User blockedUser = findByUserId(blockedId);

        if (!hasBlock(blockedUser, blocker)) {
            createBlock(blocker, blockedUser);
            return true;
        } else {
            removeBlock(blocker, blockedUser);
            return false;
        }
    }

    private void removeBlock(User blocker, User blockedUser) {
        blockRepository.findByBlockerAndBlocked(blocker, blockedUser)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    private void createBlock(User blocker, User blockedUser) {
        blockRepository.save(new Block(blocker, blockedUser));
    }

    private boolean hasBlock(User blockedUser, User blocker) {
        return blockRepository.existsByBlockerAndBlocked(blockedUser, blocker);
    }



    @Transactional
    public void createBlock(User blocker, Long blockedId) {

        User blocked = findByUserId(blockedId);

        blockRepository.save(new Block(blocker, blocked));
    }

    @Transactional
    public void removeBlock(User blocker, Long blockedId) {
        User blocked = findByUserId(blockedId);
        Block block = findByBlockerAndBlocked(blocker, blocked);

        blockRepository.delete(block);
    }

    
    public List<BlockSimpleDto> blockList(User user) {
        return blockRepository.findByBlocker(user).stream().map(block -> BlockSimpleDto.toDto(block.getBlocked())).toList();
    }




}
