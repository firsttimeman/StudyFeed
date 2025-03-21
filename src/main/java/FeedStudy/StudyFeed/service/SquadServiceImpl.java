package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.dto.SquadUpdateRequestDto;
import FeedStudy.StudyFeed.entity.User;

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
