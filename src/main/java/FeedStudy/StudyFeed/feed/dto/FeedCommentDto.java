package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedCommentDto {


    private Long id;
    private Long parentId;
    private String nickName;
    private String profileImageUrl;
    private String content;
    private String dateTime;
    private boolean mine;
    private boolean deleted;

    // 루트 댓글 전용
    private Integer replyCount;            // 대댓글 총 개수
    private List<FeedCommentDto> previewReplies; // 미리보기 대댓글 2개
    private Boolean hasMoreReplies;        // “답글 더보기” 노출 여부

    private static final DateTimeFormatter F =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String fmt(LocalDateTime t) {
        return t == null ? LocalDateTime.now().format(F) : t.format(F);
    }


    /** 루트 댓글용 팩토리 */
    public static FeedCommentDto forRoot(FeedComment root,
                                         List<FeedCommentDto> previewReplies,
                                         Long currentUserId) {
        boolean deleted = root.isDeleted();
        String nick = deleted ? null : root.getUser().getNickName();
        String profile = deleted ? null : root.getUser().getImageUrl();
        boolean mine = root.getUser() != null && root.getUser().getId().equals(currentUserId);
        int replyCount = root.getReplyCount();
        boolean hasMore = replyCount > (previewReplies == null ? 0 : previewReplies.size());

        return new FeedCommentDto(
                root.getId(),
                null,
                nick,
                profile,
                root.getContent(),
                fmt(root.getCreatedAt()),
                mine,
                deleted,
                replyCount,
                previewReplies,
                hasMore
        );
    }

    /** 대댓글용 팩토리 */
    public static FeedCommentDto forReply(FeedComment reply,
                                          Long parentId,
                                          Long currentUserId) {
        boolean deleted = reply.isDeleted();
        String nick = deleted ? null : reply.getUser().getNickName();
        String profile = deleted ? null : reply.getUser().getImageUrl();
        boolean mine = reply.getUser() != null && reply.getUser().getId().equals(currentUserId);

        return new FeedCommentDto(
                reply.getId(),
                parentId,
                nick,
                profile,
                reply.getContent(),
                fmt(reply.getCreatedAt()),
                mine,
                deleted,
                null,
                null,
                null
        );
    }


}


