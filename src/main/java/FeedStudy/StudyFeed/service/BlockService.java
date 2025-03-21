package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.entity.Feed.Block;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.exception.ErrorCode;
import FeedStudy.StudyFeed.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.repository.BlockRepository;
import FeedStudy.StudyFeed.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;

    @Transactional
    public boolean updateBlock(User blocker, Long blockedId) {
        User blockedUser = userRepository.findById(blockedId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

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

    public void createBlock(User blocker, Long blockedId) {
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        blockRepository.save(new Block(blocker, blocked));
    }

    public void removeBlock(User blocker, Long blockedId) {
        User blocked = userRepository.findById(blockedId).orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        Block block = blockRepository.findByBlockerAndBlocked(blocker, blocked).orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        blockRepository.delete(block);
    }

    public List<BlockSimpleDto> blockList(User user) {
        return blockRepository.findByBlocker(user).stream().map(block -> BlockSimpleDto.toDto(block.getBlocked())).toList();
    }




}
