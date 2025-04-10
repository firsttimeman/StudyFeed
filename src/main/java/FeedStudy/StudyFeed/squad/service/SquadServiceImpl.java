package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.squad.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.squad.dto.SquadUpdateRequestDto;
import FeedStudy.StudyFeed.user.entity.User;

public interface SquadServiceImpl {


    void createSquad(SquadCreateRequestDto requestDto, User user);

    void updateSquad(Long squadId, User user, SquadUpdateRequestDto requestDto);

    void deleteSquad(Long squadId, User user);

    void joinSquad(Long squadId, User user);

    void approveMember(Long squadId, User user);

    void rejectMember(Long squadId, User user);

    void closeRecruitment(Long squadId, User user);

    void kickMember(Long squadId, User leader, Long targetId);

    void leaveSquad(Long squadId, User user);

    void reportMember(Long squadId, User reporter, Long reportedId, String reason);







}
