package FeedStudy.StudyFeed.global.config;

import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if(token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

               try {
                   Claims claims = jwtUtil.getClaimsFromToken(token);
                   String email = claims.getSubject();

                   userRepository.findByEmail(email).ifPresent(user -> {

                       UsernamePasswordAuthenticationToken authenticationToken =
                               new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                       accessor.setUser(authenticationToken);
                   });
               } catch (Exception e) {
                   System.out.println("인증실패: " + e.getMessage());
               }

            }
        }
        return message;
    }
}
