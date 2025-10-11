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

        if("/api/auth/refresh".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        try {
            if(header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);


                Claims claims = jwtUtil.validateToken(token);
                Object type = claims.get("token_type");
                if(!"access".equals(type)) {
                    chain.doFilter(request, response);
                    return;
                }

                String email = claims.getSubject();
                userRepository.findByEmail(email).ifPresent(user -> {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }


        chain.doFilter(request, response);

    }
}
