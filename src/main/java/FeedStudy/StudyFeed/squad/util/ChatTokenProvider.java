package FeedStudy.StudyFeed.squad.util;

import FeedStudy.StudyFeed.openchat.entity.ChatRoom;
import FeedStudy.StudyFeed.openchat.repository.ChatRoomRepository;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ChatTokenProvider {


    @Value("${chatjwt.secretKey}")
    private String secretKey;



    public String createSquadChatToken(User user, Squad squad) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 5 * 60 * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("squadId", squad.getId())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }


    public String createOpenChatToken(User user, ChatRoom chatRoom) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 5 * 60 * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("chatRoomId", chatRoom.getId())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public Claims validateChatToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 채팅 토큰입니다");
        }
    }
}
