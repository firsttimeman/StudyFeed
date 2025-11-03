package FeedStudy.StudyFeed.block.service;

import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {


    @Mock
    private BlockRepository blockRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlockService blockService;

    @Test
    public void 정상적으로_블록에_성공한다() {
        /**
         * 호출됐는지 판단하는 메서드,
         * 반환값이 없기 때문에 이게 호출되면 정상적으로 테스트가 통과되었다고 가정할 수 있다
         */
        BDDMockito.verify(blockRepository.save(new Block()));
    }

    @Test
    public void 유저를_찾을_수_없는_경우_MemberException_이_발생한다() {

    }


    @Test
    public void 같은_유저_인_경우_BLOCK_SELF_NOT_ALLOWED_이_발생한다() {

    }
}