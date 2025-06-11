package FeedStudy.StudyFeed.block.service;

import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.block.entity.Block;
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


    @Transactional
    public void createBlock(User user, User other) {

        if(blockRepository.findByBlockerAndBlocked(user, other).isPresent()) {
            throw new IllegalArgumentException("이미 차단된 사용자 입니다.");
        }

        if(user.getId() == other.getId()) {
            throw new IllegalArgumentException("자기 자신을 차단할수 없습니다.");
        }

        Block block = new Block(user, other);
        blockRepository.save(block);
    }

    @Transactional
    public void removeBlock(User user, User other) {
        Block block = blockRepository.findByBlockerAndBlocked(user, other)
                .orElseThrow(() -> new IllegalArgumentException("차단되어 있지 않습니다."));

        blockRepository.delete(block);
    }

    
    public List<BlockSimpleDto> blockList(User user) {
        return blockRepository.findByBlocker(user).stream().map(block -> BlockSimpleDto.toDto(block.getBlocked())).toList();
    }




}
