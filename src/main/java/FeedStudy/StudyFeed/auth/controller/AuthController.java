package FeedStudy.StudyFeed.auth.controller;

import FeedStudy.StudyFeed.auth.service.AuthService;
import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/verifymail")
    public ResponseEntity<?> verifymail(@RequestBody SignUpRequestDto req) {
        authService.sendVerifyMail(req);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequestDto req) {
        authService.signUp(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestParam String email, @RequestParam String snsType,
                                    @RequestParam String snsId) {
        Map<String, String> token = authService.login(email, snsType, snsId);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/repassword")
    public ResponseEntity<?> rePassword(@RequestParam String email, @RequestParam String rawPassword) {
        authService.resetPassword(email, rawPassword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal User user) {
        String refreshToken = authService.refreshToken(user);
        return ResponseEntity.ok(refreshToken);
    }




}
