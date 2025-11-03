package FeedStudy.StudyFeed.block.service;

import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.global.exception.exceptiontype.BlockException;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService   {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;


    @Transactional
    public void createBlock(User user, Long otherId) {

        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));


        if(user.getId().equals(other.getId())) {
            throw new BlockException(ErrorCode.BLOCK_SELF_NOT_ALLOWED);
        }


        if(blockRepository.findByBlockerAndBlocked(user, other).isPresent()) {
            throw new BlockException(ErrorCode.BLOCK_ALREADY_EXISTS);
        }


        Block block = new Block(user, other);
        blockRepository.save(block);
    }

    @Transactional
    public void removeBlock(User user, Long otherId) {
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        Block block = blockRepository.findByBlockerAndBlocked(user, other)
                .orElseThrow(() -> new BlockException(ErrorCode.BLOCK_NOT_FOUND));

        blockRepository.delete(block);
    }

    
    public List<BlockSimpleDto> blockList(User user) {
        return blockRepository.findByBlockerWithBlocked(user).stream()
                .map(block -> BlockSimpleDto.toDto(block.getBlocked()))
                .toList();
    }
    // todo n+1 문제 발생
    /*
    	•	findByBlocker(user)가 List<Block>만 조회하고, Block.blocked가 @ManyToOne(fetch = LAZY)라면
        .map(block -> block.getBlocked()) 시 차단 대상 유저를 항목마다 추가 조회해서 N+1이 됩니다.
     */




}
