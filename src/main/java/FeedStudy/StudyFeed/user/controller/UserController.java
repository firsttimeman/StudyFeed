package FeedStudy.StudyFeed.user.controller;

import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
//
//    @PostMapping("/signup")
//    public ResponseEntity<String> register(@Valid @RequestParam String email) throws MessagingException {
//        userService.RegisterUser(email);
//        return ResponseEntity.ok("이메일이 보내졌습니다 이메일을 통해 인증을 완료하세요");
//    }
//
//
//
//
//    @PostMapping("/approve")
//    public ResponseEntity<String> approve(@Valid @RequestBody SignUpRequestDto request) {
//        userService.activateUser(request);
//        return ResponseEntity.ok("회원 가입이 완료되었습니다.");
//    }
//
//
//    @PostMapping("/signin")
//    public ResponseEntity<?> login(@Valid @RequestParam String email, String snsType, String snsId) {
//        Map<String, String> tokens = userService.login(email, snsType, snsId);
//        return ResponseEntity.ok()
//                .body(tokens);
//    }
//
//    @PostMapping("/testlogout")
//    public ResponseEntity<?> logout(@RequestHeader( value = "Authorization", required = false) String token) {
//
//        log.info("로그아웃 요청 받음: {}", token);
//
//        userService.logout(token);
//
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "로그아웃 되었습니다.");
//
//        log.info("로그아웃 응답 바디: {}", response);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(response);
//    }




    /**
     * 닉네임 null 일시 새로 만들기
     */
    @GetMapping("/generate_nickname")
    public ResponseEntity<String> makeNickName(@AuthenticationPrincipal User user) {
        userService.makeNickName(user);
        return ResponseEntity.ok("NickName 생성 완료");
    }

    @PutMapping("/update_nickname")
    public ResponseEntity<String> updateNickName(@AuthenticationPrincipal User user, @RequestParam String nickname) {
        userService.updateNickname(user, nickname);
        return ResponseEntity.ok("NickName 적용 완료");
    }

    //필요한거 같아서 만들기는 했는데 왜 필요하지?
    @PostMapping("/limit_nickname")
    public ResponseEntity<?> limitName(@AuthenticationPrincipal User user, @RequestParam String nickname) {
        userService.limitNickname(nickname);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/has_nickname")
    public ResponseEntity<Boolean> hasNickName(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.hasNickName(user));
    }

    /**
     * 유저 fcmtoken 재발행 만들기
     */
    @PutMapping("/update_fcm")
    public ResponseEntity<String> refreshFcmToken(@RequestParam("token") String fcmToken,
                                                  @AuthenticationPrincipal User user) {

        System.out.println("update FCM token: " + fcmToken);
        userService.fcmTokenRefresh(user, fcmToken);
        return ResponseEntity.ok("FCM 토큰이 재발급 되었습니다.");
    }

    @PutMapping("/modity_password") // 사실 유저 설정창에서의 프로필 변경 느낌
    public ResponseEntity<?> changeProfile(@RequestParam String email, @RequestParam String providerType,
                                           @RequestParam String password, @RequestParam String providerId) {
        userService.changeProfile(email, providerType, password, providerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkAccessToken(@AuthenticationPrincipal User user, @RequestParam String data) {
        String check = userService.checkAccessToken(data);
        return ResponseEntity.ok().body(check);
    }



}
