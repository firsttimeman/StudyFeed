package FeedStudy.StudyFeed.global.config;

import FeedStudy.StudyFeed.global.jwt.JwtFilter;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    } // 이게 무슨 기능이였지????

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration configuration, UserService userService) throws Exception {

        AuthenticationManager authenticationManager = configuration.getAuthenticationManager();

        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/register", "/api/user/approve", "/api/user/login", "/api/user/testlogout").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout.disable())
                .formLogin().disable()
                .httpBasic().disable()
                .addFilterBefore(new JwtFilter(authenticationManager,jwtUtil, userService), UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }
}
