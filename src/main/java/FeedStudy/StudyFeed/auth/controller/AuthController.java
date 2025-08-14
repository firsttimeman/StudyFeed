package FeedStudy.StudyFeed.auth.controller;

import FeedStudy.StudyFeed.auth.service.AuthService;
import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "auth api", description = "auth 관련 api")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/verifymail")
    @Operation(summary = "인증 이메일 발송", description = "인증 이메일을 발송합니다.")
    public ResponseEntity<?> verifymail(@Parameter(description = "이메일 주소", required = true) @RequestParam String email) {
        authService.sendVerifyMail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check_auth_code")
    public ResponseEntity<?> checkAuthCode(@RequestParam String email, @RequestParam String authCode) {
        authService.checkAuthCode(email, authCode);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "최종적으로 회원가입을 마무리 하는 곳입니다.")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequestDto req) {
        authService.signUp(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "로그인을 진행합니다.")
    public ResponseEntity<?> signin(@Parameter(description = "유저 이메일") @RequestParam String email,
                                    @Parameter(description = "유저 소셜 종류") @RequestParam String snsType,
                                    @Parameter(description = "유저 고유 소셜 아이디") @RequestParam String snsId) {
        Map<String, String> token = authService.login(email, snsType, snsId);
        return ResponseEntity.ok(token);
    }

    //근데 이게 없네?
//    @PostMapping("/repassword")
//    @Operation(summary = "패스워드 재설정", description = "패스워드를 재 설정을 합니다.")
//    public ResponseEntity<?> rePassword(@Parameter(description = "유저 이메일") @RequestParam String email,
//                                        @Parameter(description = "유저 재설정할 비밀번호") @RequestParam String rawPassword) {
//        authService.resetPassword(email, rawPassword);
//        return ResponseEntity.ok().build();
//    }


    @PostMapping("/refresh")
    @Operation(summary = "Jwt Refresh 토큰을 재발급합니다", description = "Jwt Refresh 토큰을 재발급합니다")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal User user) {
        String refreshToken = authService.refreshToken(user);
        return ResponseEntity.ok(refreshToken);
    }




}
