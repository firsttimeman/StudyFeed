package FeedStudy.StudyFeed.controller;

import FeedStudy.StudyFeed.dto.LoginRequestDto;
import FeedStudy.StudyFeed.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestParam String email) throws MessagingException {
        userService.RegisterUser(email);
        return ResponseEntity.ok("이메일이 보내졌습니다 이메일을 통해 인증을 완료하세요");
    }

    @PostMapping("/approve")
    public ResponseEntity<String> approve(@Valid @RequestBody SignUpRequestDto request) {
        userService.activateUser(request);
        return ResponseEntity.ok("회원 가입이 완료되었습니다.");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        Map<String, String> tokens = userService.login(request);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.get("accessToken"))
                .header("Refresh-Token", tokens.get("refreshToken"))
                .body("로그인이 완료되었습니다.");

    }

    @PostMapping("/testlogout")
    public ResponseEntity<?> logout(@RequestHeader( value = "Authorization", required = false) String token) {

        log.info("로그아웃 요청 받음: {}", token);

        userService.logout(token);

        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃 되었습니다.");

        log.info("로그아웃 응답 바디: {}", response);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }


}
