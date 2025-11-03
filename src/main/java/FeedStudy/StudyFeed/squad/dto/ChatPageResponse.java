package FeedStudy.StudyFeed.squad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatPageResponse {
    private List<SquadChatItemDto> items; // 채팅 메시지 목록
    private boolean hasNext;              // 다음 페이지가 있는지
    private Long nextCursor;              // 다음 요청 시 before=nextCursor 로 사용
}