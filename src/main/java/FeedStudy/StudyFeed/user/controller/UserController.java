package FeedStudy.StudyFeed.user.controller;

import FeedStudy.StudyFeed.user.dto.DescriptionRequestDto;
import FeedStudy.StudyFeed.user.dto.ProfileImageUpdateDto;
import FeedStudy.StudyFeed.user.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    //
    // @PostMapping("/signup")
    // public ResponseEntity<String> register(@Valid @RequestParam String email)
    // throws MessagingException {
    // userService.RegisterUser(email);
    // return ResponseEntity.ok("이메일이 보내졌습니다 이메일을 통해 인증을 완료하세요");
    // }
    //
    //
    //
    //
    // @PostMapping("/approve")
    // public ResponseEntity<String> approve(@Valid @RequestBody SignUpRequestDto
    // request) {
    // userService.activateUser(request);
    // return ResponseEntity.ok("회원 가입이 완료되었습니다.");
    // }
    //
    //
    // @PostMapping("/signin")
    // public ResponseEntity<?> login(@Valid @RequestParam String email, String
    // snsType, String snsId) {
    // Map<String, String> tokens = userService.login(email, snsType, snsId);
    // return ResponseEntity.ok()
    // .body(tokens);
    // }
    //
    // @PostMapping("/testlogout")
    // public ResponseEntity<?> logout(@RequestHeader( value = "Authorization",
    // required = false) String token) {
    //
    // log.info("로그아웃 요청 받음: {}", token);
    //
    // userService.logout(token);
    //
    // Map<String, String> response = new HashMap<>();
    // response.put("message", "로그아웃 되었습니다.");
    //
    // log.info("로그아웃 응답 바디: {}", response);
    //
    // return ResponseEntity.ok()
    // .contentType(MediaType.APPLICATION_JSON)
    // .body(response);
    // }

    @Operation(summary = "랜덤 닉네임 생성")
    @GetMapping("/generate_nickname")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> makeNickName() {
        userService.makeNickName();
        return ResponseEntity.ok("NickName 생성 완료");
    }

    @Operation(summary = "닉네임 수정")
    @PutMapping("/update_nickname")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updateNickName(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "수정할 닉네임") @RequestParam String nickname) {
        userService.updateNickname(user, nickname);
        return ResponseEntity.ok("NickName 적용 완료");
    }

    @Operation(summary = "닉네임 제한 확인")
    @PostMapping("/limit_nickname")
    public ResponseEntity<?> limitName(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "제한할 닉네임") @RequestParam String nickname) {
        userService.limitNickname(nickname);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "닉네임 보유 여부 확인")
    @GetMapping("/has_nickname")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> hasNickName(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.hasNickName(user));
    }

    @Operation(summary = "FCM 토큰 재발급")
    @PutMapping("/update_fcm")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> refreshFcmToken(
            @RequestParam("token") String fcmToken,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        System.out.println("update FCM token: " + fcmToken);
        userService.fcmTokenRefresh(user, fcmToken);
        return ResponseEntity.ok("FCM 토큰이 재발급 되었습니다.");
    }

    @Operation(summary = "비밀번호 및 프로필 정보 수정")
    @PutMapping("/modity_password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changeProfile(
            @RequestParam String email,
            @RequestParam String providerType,
            @RequestParam String password,
            @RequestParam String providerId) {
        userService.changeProfile(email, providerType, password, providerId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Access Token 유효성 검사")
    @GetMapping("/check")
    // @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> checkAccessToken(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(userService.getNickname(user));
    }

    @Operation(summary = "유저 자기소개 수정")
    @PutMapping("/modify_description")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> modifyDescription(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestBody DescriptionRequestDto dto) {
        User user1 = userService.modifyDescription(dto, user);
        return ResponseEntity.ok().body(user1);
    }

    @Operation(summary = "프로필 이미지 수정")
    @PostMapping("/profile-image")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @ModelAttribute ProfileImageUpdateDto dto) {
        User updated = userService.changeProfileImage(user, dto);
        return ResponseEntity.ok(Map.of("profileImageUrl", updated.getImageUrl()));
    }

    @Operation(summary = "알림 전체 설정 토글")
    @PostMapping("/alarm-settings/toggle")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> toggleAlarm(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.toggleAllAlarm(user, enabled));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteUser(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        userService.deleteUser(user);
        return ResponseEntity.ok().build();
    }

}
