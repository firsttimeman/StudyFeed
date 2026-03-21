package FeedStudy.StudyFeed.auth.service;

import FeedStudy.StudyFeed.auth.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.AuthCodeException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.TokenException;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.jwt.UserPrincipal;
import FeedStudy.StudyFeed.auth.dto.CheckAuthCodeDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.auth.repository.BlackListRepository;
import FeedStudy.StudyFeed.auth.repository.RefreshRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.user.service.MailService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthCodeService authCodeService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final BlackListRepository blackListRepository;
    private final AuthenticationManager authenticationManager;

    //todo 로그아웃 기능이 없네?

    public void sendVerifyMail(String email) {

        if(userRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String authCode = authCodeService.generateAuthCode();
        authCodeService.saveAuthCode(email, authCode);
        mailService.sendVerifyMail(email, authCode);

    }

    public void checkAuthCode(CheckAuthCodeDto req) {
        boolean success =
                authCodeService.checkAuthCode(req.getEmail(), req.getAuthCode());
        if (!success) {
            throw new AuthCodeException(ErrorCode.AUTH_CODE_MISMATCH);
        }
    }

    public void signUp(SignUpRequestDto req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new MemberException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        authCodeService.validateVerified(req.getEmail());

        String imageName = "avatar_placeholder.png";
        User user = new User(req, passwordEncoder.encode(req.getPassword()), imageName);
        userRepository.save(user);

        authCodeService.removeVerified(req.getEmail());
    }

    public Map<String, String> login(String email, String password) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = principal.getUser();

            String role = user.getUserRole().name();

            String accessToken = jwtUtil.createAccessToken(user.getEmail(), role);
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), role);

            refreshRepository.saveRefreshToken(user.getEmail(), refreshToken);
            Map<String, String> map = new HashMap<>();
            map.put("accessToken", accessToken);
            map.put("refreshToken", refreshToken);
            return map;
        } catch (AuthenticationException e) {
            throw new MemberException(ErrorCode.LOGIN_FAILED);
        }

    }


    public void resetPassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.changePassword(encodedPassword);
        userRepository.save(user);
    }

    public Map<String, String> refreshToken(String refreshToken) {

        Claims claims = jwtUtil.validateToken(refreshToken);
        if(!"refresh".equals(claims.get("token_type"))) {
            throw new TokenException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = claims.getSubject();

        String storedToken = refreshRepository.findByEmail(email);
        if(storedToken == null || !storedToken.equals(refreshToken)) {
            throw new TokenException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        String role = user.getUserRole().name();

        String newAccessToken = jwtUtil.createAccessToken(email, role);
        String newRefreshToken = jwtUtil.createRefreshToken(email, role);
        refreshRepository.saveRefreshToken(email, newRefreshToken);
        log.info("Refresh Token 갱신 완료: {}", email);

        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }

    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = bearerToken.substring(7);

        Claims claims = jwtUtil.validateToken(accessToken);

        String tokenType = claims.get("token_type", String.class);
        if (!"access".equals(tokenType)) {
            throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String email = claims.getSubject();

        refreshRepository.deleteRefreshToken(email);

        long expirationTime = claims.getExpiration().getTime() - System.currentTimeMillis();

        if (expirationTime > 0) {
            blackListRepository.addToBlackList(accessToken, expirationTime);
            log.info("로그아웃 완료 - blacklist 등록: {}", email);
        }
    }

}
