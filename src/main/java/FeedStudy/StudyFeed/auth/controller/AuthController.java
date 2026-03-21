package FeedStudy.StudyFeed.auth.controller;

import FeedStudy.StudyFeed.auth.dto.SignInRequestDto;
import FeedStudy.StudyFeed.auth.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.auth.service.AuthService;
import FeedStudy.StudyFeed.auth.dto.CheckAuthCodeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> verifymail(
            @Parameter(description = "이메일 주소", required = true) @RequestParam String email) {
        authService.sendVerifyMail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check_auth_code")
    @Operation(summary = "인증 코드 검증", description = "인증 코드를 검증합니다.")
    public ResponseEntity<?> checkAuthCode(@Valid @RequestBody CheckAuthCodeDto req) {
        authService.checkAuthCode(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "최종적으로 회원가입을 마무리 하는 곳입니다.")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequestDto req) {
        authService.signUp(req);

        Map<String, String> token = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "로그인을 진행합니다.")
    public ResponseEntity<?> signin(@Valid @RequestBody SignInRequestDto req) {
        Map<String, String> token = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(token);
    }


     @PostMapping("/repassword")
     @Operation(summary = "패스워드 재설정", description = "패스워드를 재 설정을 합니다.")
     public ResponseEntity<?> rePassword(@Parameter(description = "유저 이메일")
     @RequestParam String email,
     @Parameter(description = "유저 재설정할 비밀번호") @RequestParam String rawPassword) {
     authService.resetPassword(email, rawPassword);
     return ResponseEntity.ok().build();
     }

    @PostMapping("/refresh")
    @Operation(summary = "Jwt Refresh 토큰을 재발급합니다", description = "Jwt Refresh 토큰을 재발급합니다")
    public ResponseEntity<?> refresh(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");
        if(bearer == null || !bearer.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Refresh token missing");
        }

        String refreshToken = bearer.substring(7);
        Map<String, String> tokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(tokens);
    }


    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "refresh token 삭제 및 access token 블랙리스트 처리")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        authService.logout(bearer);
        return ResponseEntity.ok().build();
    }
}
