package FeedStudy.StudyFeed.block.service;

import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.BlockException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createBlock(User user, Long otherId) {
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        if (user.getId().equals(other.getId())) {
            throw new BlockException(ErrorCode.BLOCK_SELF_NOT_ALLOWED);
        }

        if (blockRepository.existsByBlockerAndBlocked(user, other)) {
            throw new BlockException(ErrorCode.BLOCK_ALREADY_EXISTS);
        }

        blockRepository.save(new Block(user, other));
    }

    @Transactional
    public void removeBlock(User user, Long otherId) {
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        Block block = blockRepository.findByBlockerAndBlocked(user, other)
                .orElseThrow(() -> new BlockException(ErrorCode.BLOCK_NOT_FOUND));

        blockRepository.delete(block);
    }

    @Transactional
    public List<BlockSimpleDto> blockList(User user) {
        return blockRepository.findByBlockerWithBlocked(user).stream()
                .map(block -> BlockSimpleDto.toDto(block.getBlocked()))
                .toList();
    }
}