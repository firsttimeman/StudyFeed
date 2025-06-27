package FeedStudy.StudyFeed.user.service;

import FeedStudy.StudyFeed.auth.service.AuthCodeService;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.AuthCodeException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.TokenException;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.global.utils.NickNameUtils;
import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.BlackListRepository;
import FeedStudy.StudyFeed.user.repository.RefreshRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import com.google.api.pathtemplate.ValidationException;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class UserService {

    public final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthCodeService authCodeService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final BlackListRepository blackListRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

//    public void RegisterUser(String email) throws MessagingException {
//
//        String authCode = authCodeService.generateAuthCode();
//        authCodeService.saveAuthCode(email, authCode);
//        mailService.sendVerifyMail(email, authCode);
//
//        //1. 해당되는 이메일로 인증코드를 보낸다. 보내면서 레디스에서 코드를 저장한다.
//    }

//    public void activateUser(SignUpRequestDto signUpRequestDto) {
//
//        String email = signUpRequestDto.getEmail();
//        String authCode = signUpRequestDto.getAuthcode();
//
//        if (authCodeService.checkAuthCode(email, authCode)) {
//            throw new AuthCodeException(ErrorCode.AUTH_CODE_MISMATCH);
//        }
//
//        if (userRepository.existsByEmail(email)) {
//            throw new MemberException(ErrorCode.EMAIL_ALREADY_EXISTS);
//        }
//
//        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getProviderType() + signUpRequestDto.getProviderId());
//
//        User newUser = User.builder()
//                .email(email)
//                .password(encodedPassword)
//                .userRole(UserRole.USER)
//                .providerType(signUpRequestDto.getProviderType())
//                .providerId(signUpRequestDto.getProviderId())
//                .telecom(signUpRequestDto.getTelecom())
//                .gender(signUpRequestDto.getGender())
////                .nickName(signUpRequestDto.getNickName())
//                .birthDate(signUpRequestDto.getBirthDate()) // feed 알람도 추가해야 하는지 Todo
//                .build();
//
//
//        userRepository.save(newUser);
//    }
//    // 1. 이메일과 코드를 가지고 와서 비교하면서 코드가 틀리면 예외 발생. 이메일이 이미 존재하는 이메일일시 예외 밣생
//    // 2. 없으면 새로운 회원을 가입시킴
//
//    public Map<String, String> login(String email, String snsType, String snsId) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
//
//        if (!passwordEncoder.matches(snsType + snsId, user.getPassword())) {
//            throw new MemberException(ErrorCode.PASSWORD_NOT_MATCH);
//        }
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(email, snsType + snsId)
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String role = user.getUserRole().name();
//        String accessToken = jwtUtil.createAccessToken(user.getEmail(), role);
//        String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), role);
//        refreshRepository.saveRefreshToken(user.getEmail(), refreshToken);
//
//
//        Map<String, String> map = new HashMap<>();
//        map.put("accessToken", accessToken);
//        map.put("refreshToken", refreshToken);
//        return map;
//    }

//    public void logout(String accessToken) {
//
//        String token = accessToken.replace("Bearer ", ""); // Bearer 제거
//
//
//        Claims claims;
//        try {
//            claims = jwtUtil.validateToken(token);
//        } catch (Exception e) {
//            throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
//        }
//
//        String email = claims.getSubject();
//
//        log.info("✅ 삭제 전 refresh token 조회: {}", refreshRepository.findByEmail(email));
//
//        refreshRepository.deleteRefreshToken(email);
//
//        log.info("✅ 삭제 후 refresh token 조회: {}", refreshRepository.findByEmail(email));
//
//
//        long tokenExpiration = jwtUtil.getTokenExpiration(token);
//        System.out.println("🔴 토큰 만료 시간(ms): " + tokenExpiration);
//
//        if (tokenExpiration > 0) {
//            blackListRepository.addToBlackList(token, tokenExpiration);
//            log.info("🛑 블랙리스트 추가 완료: {}", token);
//        } else {
//            log.warn("⚠ 블랙리스트에 추가되지 않음: 만료 시간이 0 이하");
//        }
//    }

    @Transactional
    public void fcmTokenRefresh(User user, String fcmToken) {
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    public String makeNickName(User user) {


        String generateNickName = generateUniqueNickName();
        return generateNickName;
    }

    public void updateNickname(User user, String nickName) {

        if(userRepository.existsByIdNotAndNickName(user.getId(), nickName)) {
            throw new MemberException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.setNickName(nickName);
        userRepository.save(user);
    }


    private String generateUniqueNickName() {
        String nickname;
        int attempt = 0;
        do {
            nickname = NickNameUtils.generateNickname();
            attempt++;
            if (attempt > 10) {
                throw new MemberException(ErrorCode.NICKNAME_GENERATION_FAILED);
            }
        } while (userRepository.existsByNickName(nickname));
        return nickname;
    }


    public Boolean hasNickName(User user) {
        return !(user.getNickName() == null || user.getNickName().equals(""));
    }


    public String limitNickname(String nickname) {
        String regex = "^[a-zA-Z0-9가-힣\\s]{2,8}$";
        if (!nickname.matches(regex)) {
            throw new ValidationException("올바르지 않은 형식의 이름입니다");
        } else {
            return "올바른 형식의 이름입니다";
        }
    }


    public void changeProfile(String email, String providerType, String providerId, String password) {
        User user = userRepository.findByEmail(email).orElseThrow();
        String encode = passwordEncoder.encode(password);
        user.setProviderType(providerType);
        user.setProviderId(providerId);
        user.setPassword(encode);
        userRepository.save(user);
    }

    public String checkAccessToken(String data) {

        return data + "okay token";
    }
}
