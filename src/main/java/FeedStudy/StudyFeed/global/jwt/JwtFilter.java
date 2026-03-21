package FeedStudy.StudyFeed.global.jwt;

import FeedStudy.StudyFeed.auth.repository.BlackListRepository;
import FeedStudy.StudyFeed.user.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final BlackListRepository blackListRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader("Authorization");

        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (blackListRepository.isTokenBlackListed(token)) {
                    chain.doFilter(request, response);
                    return;
                }

                Claims claims = jwtUtil.validateToken(token);

                String tokenType = claims.get("token_type", String.class);

                // access token만 인증에 사용
                if ("access".equals(tokenType)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                    String email = claims.getSubject();

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.debug("JWT 인증 처리 실패", e);
        }

        chain.doFilter(request, response);
    }
}
