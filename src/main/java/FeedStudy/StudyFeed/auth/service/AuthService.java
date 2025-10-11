package FeedStudy.StudyFeed.auth.service;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.AuthCodeException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.TokenException;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.user.dto.CheckAuthCodeDto;
import FeedStudy.StudyFeed.auth.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.BlackListRepository;
import FeedStudy.StudyFeed.user.repository.RefreshRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.user.service.MailService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    public final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthCodeService authCodeService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final BlackListRepository blackListRepository;
    private final AuthenticationManager authenticationManager;

    public void sendVerifyMail(String email) {

        String authCode = authCodeService.generateAuthCode();
        authCodeService.saveAuthCode(email, authCode);

        mailService.sendVerifyMail(email, authCode);

    }

    public void checkAuthCode(CheckAuthCodeDto req) {
        if (!authCodeService.checkAuthCode(req.getEmail(), req.getCode())) {
            throw new AuthCodeException(ErrorCode.AUTH_CODE_MISMATCH);
        }
    }

    public void signUp(SignUpRequestDto req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new MemberException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String imageName = "avatar_placeholder.png";
        User user = new User(req, passwordEncoder.encode(req.getProviderType() + req.getProviderId()), imageName);
        System.out.println(user);
        userRepository.save(user);
    }

    public Map<String, String> login(String email, String providerType, String providerId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        System.out.println(user != null);
        System.out.println(email + " " + providerType + " " + providerId);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, providerType + providerId));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = user.getUserRole().name();
        String accessToken = jwtUtil.createAccessToken(user.getEmail(), role);
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), role);
        refreshRepository.saveRefreshToken(user.getEmail(), refreshToken);

        Map<String, String> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
    }

    // public void logout(String accessToken) {
    //
    // String token = accessToken.replace("Bearer ", ""); // Bearer 제거
    //
    //
    // Claims claims;
    // try {
    // claims = jwtUtil.validateToken(token);
    // } catch (Exception e) {
    // throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
    // }
    //
    // String email = claims.getSubject();
    //
    // log.info("✅ 삭제 전 refresh token 조회: {}",
    // refreshRepository.findByEmail(email));
    //
    // refreshRepository.deleteRefreshToken(email);
    //
    // log.info("✅ 삭제 후 refresh token 조회: {}",
    // refreshRepository.findByEmail(email));
    //
    //
    // long tokenExpiration = jwtUtil.getTokenExpiration(token);
    // System.out.println("🔴 토큰 만료 시간(ms): " + tokenExpiration);
    //
    // if (tokenExpiration > 0) {
    // blackListRepository.addToBlackList(token, tokenExpiration);
    // log.info("🛑 블랙리스트 추가 완료: {}", token);
    // } else {
    // log.warn("⚠ 블랙리스트에 추가되지 않음: 만료 시간이 0 이하");
    // }
    // }

    public void resetPassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    public Map<String, String> refreshToken(String refreshToken) {

        Claims claims = jwtUtil.validateToken(refreshToken);
        if(!"refresh".equals(claims.get("token_type"))) {
            throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String email = claims.getSubject();

        String storedToken = refreshRepository.findByEmail(email);
        if(storedToken == null || !storedToken.equals(refreshToken)) {
            throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        String role = user.getUserRole().name();

        String newAccessToken = jwtUtil.createAccessToken(email, role);
        String newRefreshToken = jwtUtil.createRefreshToken(email, role);
        refreshRepository.saveRefreshToken(email, newRefreshToken);
        log.info("♻️ Refresh Token 갱신 완료: {}", email);

        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }

}
