package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.squad.dto.SquadMessageDto;
import FeedStudy.StudyFeed.squad.dto.SquadMessageRequest;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadChatMessage;
import FeedStudy.StudyFeed.squad.repository.SquadChatRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SquadChatService {

    private final SquadChatRepository squadChatRepository;
    private final SquadRepository squadRepository;
    private final UserRepository userRepository;

    public SquadMessageDto saveMessage(SquadMessageRequest request) {
        Long squadId = request.getSquadId();
        Squad squad = squadRepository.findById(squadId).orElse(null);
        User sender = userRepository.findById(request.getSenderId()).orElse(null);
        String content = request.getContent();

        SquadChatMessage message = squadChatRepository.save(new SquadChatMessage(squad, sender, content));
        return SquadMessageDto.toDto(message);
    }

//    public List<Squad>
}
