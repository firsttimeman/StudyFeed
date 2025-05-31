package FeedStudy.StudyFeed.global.jwt;

import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.user.service.CustomUserDetailsService;
import FeedStudy.StudyFeed.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");

        try {
            if(header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if(request.getRequestURI().equals("/api/auth/refresh")) {
                    jwtUtil.validateToken(token);
                    chain.doFilter(request, response);
                }


                Claims claims = jwtUtil.validateToken(token);
                String username = claims.getSubject();
                userRepository.findByEmail(username).ifPresent(user -> {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }


//        String email = jwtUtil.getClaimsFromToken(token).getSubject();
////        UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(email);
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을수 없습니다."));
//
//        UsernamePasswordAuthenticationToken authentication =
//                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
        chain.doFilter(request, response);

    }
}
