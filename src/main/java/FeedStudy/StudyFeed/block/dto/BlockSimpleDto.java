package FeedStudy.StudyFeed.block.dto;

import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockSimpleDto {
    private Long id;
    private String gender;
    private String nickName;

    public static BlockSimpleDto toDto(User user) {
        return new BlockSimpleDto(user.getId(), user.getGender().getDescription(), user.getNickName());
    }
}
