package FeedStudy.StudyFeed.global.config;

import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.jwt.UserPrincipal;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())
            || StompCommand.SEND.equals(accessor.getCommand())) {

            String bearer = first(accessor.getNativeHeader("Authorization"));
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);
                String email = jwtUtil.getUserEmail(token); // JwtUtil에 맞게
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    var principal = new UserPrincipal(user); // 프로젝트 타입에 맞게
                    var auth = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
                    accessor.setUser(auth); // ★ 여기서 Principal 주입
                }
            }
        }
        return message;
    }

    private static String first(List<String> v) {
        return (v == null || v.isEmpty()) ? null : v.get(0);
    }
}