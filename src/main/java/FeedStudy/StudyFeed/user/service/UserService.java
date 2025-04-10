package FeedStudy.StudyFeed.user.service;

import FeedStudy.StudyFeed.user.dto.LoginRequestDto;
import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.exceptiontype.AuthCodeException;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.TokenException;
import FeedStudy.StudyFeed.global.jwt.CustomUserDetails;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.user.repository.BlackListRepository;
import FeedStudy.StudyFeed.user.repository.RefreshRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.global.type.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. 유저 회원가입
 * 2. 유저 중복확인, 전화번호 중복확인 필요
 * 3. 회원가입이 정상적이면 이메일로 계정 활성화 링크를 보내 활성을 해야 로그인 가능
 * <p>
 * 1. 로그인
 * 이메일, 비밀번호로 로그인(JWT 발행)
 * <p>
 * 1. 비밀번호 찾기
 * 이메일로 비밀번호 재설정 페이지 보내거나, 임시 비밀번호 발급
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    public final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthCodeService authCodeService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final RefreshRepository  refreshRepository;
    private final BlackListRepository blackListRepository;

    public void RegisterUser(String email) throws MessagingException {

        String authCode = authCodeService.generateAuthCode();
        authCodeService.saveAuthCode(email, authCode);


        mailService.sendVerifyMail(email, authCode);

    }

    public void activateUser(SignUpRequestDto signUpRequestDto) {

        String email = signUpRequestDto.getEmail();
        String authCode = signUpRequestDto.getAuthcode();

        if (authCodeService.checkAuthCode(email, authCode)) {
            throw new AuthCodeException(ErrorCode.AUTH_CODE_MISMATCH);
        }

        if(userRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());

        User newUser = User.builder()
                .email(email)
                .password(encodedPassword)
                .userRole(UserRole.USER)
                .telecom(signUpRequestDto.getTelecom())
                .gender(signUpRequestDto.getGender())
                .nickName(signUpRequestDto.getNickName())
                .birthDate(signUpRequestDto.getBirthDate()) // feed 알람도 추가해야 하는지 Todo
                .build();



        userRepository.save(newUser);
    }

    public Map<String, String> login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new MemberException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        String role = user.getUserRole().name();
        String accessToken = jwtUtil.createAccessToken(user.getEmail(), role);
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), role);
        refreshRepository.saveRefreshToken(user.getEmail(), refreshToken);


        Map<String, String> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
    }

    public void logout(String accessToken) {

        String token = accessToken.replace("Bearer ", ""); // Bearer 제거


        if (!jwtUtil.validateToken(token)) {
            throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        Claims claims = jwtUtil.getClaimsFromToken(token);
        String email = claims.getSubject();

        log.info("✅ 삭제 전 refresh token 조회: {}", refreshRepository.findByEmail(email));

        refreshRepository.deleteRefreshToken(email);

        log.info("✅ 삭제 후 refresh token 조회: {}", refreshRepository.findByEmail(email));


        long tokenExpiration = jwtUtil.getTokenExpiration(token);
        System.out.println("🔴 토큰 만료 시간(ms): " + tokenExpiration);

        if (tokenExpiration > 0) {
            blackListRepository.addToBlackList(token, tokenExpiration);
            log.info("🛑 블랙리스트 추가 완료: {}", token);
        } else {
            log.warn("⚠ 블랙리스트에 추가되지 않음: 만료 시간이 0 이하");
        }
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        return new CustomUserDetails(user);
    }
}
