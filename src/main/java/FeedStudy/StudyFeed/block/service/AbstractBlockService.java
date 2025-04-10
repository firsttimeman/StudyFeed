package FeedStudy.StudyFeed.block.service;

import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractBlockService implements BlockServiceImpl{
    protected final BlockRepository blockRepository;
    protected final UserRepository userRepository;


    protected User findByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    protected Block findByBlockerAndBlocked(User blocker, User blocked) {
        return blockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

    }



}
