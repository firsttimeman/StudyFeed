package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private Integer replyCount;
    private List<FeedCommentDto> previewReplies;
    private Boolean hasMoreReplies;

    private static final DateTimeFormatter F =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(F);
    }

    public static FeedCommentDto forRoot(FeedComment root,
                                         List<FeedCommentDto> previewReplies,
                                         Long currentUserId) {
        boolean deleted = root.isDeleted();
        User author = root.getUser();

        String nick = (deleted || author == null) ? null : author.getNickName();
        String profile = (deleted || author == null) ? null : author.getImageUrl();
        boolean mine = author != null && author.getId().equals(currentUserId);

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

    public static FeedCommentDto forReply(FeedComment reply,
                                          Long parentId,
                                          Long currentUserId) {
        boolean deleted = reply.isDeleted();
        User author = reply.getUser();

        String nick = (deleted || author == null) ? null : author.getNickName();
        String profile = (deleted || author == null) ? null : author.getImageUrl();
        boolean mine = author != null && author.getId().equals(currentUserId);

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