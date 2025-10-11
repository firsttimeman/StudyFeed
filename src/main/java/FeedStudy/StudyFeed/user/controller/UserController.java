package FeedStudy.StudyFeed.user.controller;

import FeedStudy.StudyFeed.user.dto.*;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
 

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
    @GetMapping("/limit_nickname")
    public ResponseEntity<?> limitName(
            @Parameter(description = "제한할 닉네임") @RequestParam String nickname) {
        NickNameCheckResponse resp = userService.checkNickname(nickname);
        return ResponseEntity.ok(resp);
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
            @RequestBody FcmTokenRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        System.out.println("update FCM token: " + request.fcmToken());
        userService.fcmTokenRefresh(user, request.fcmToken());
        return ResponseEntity.ok("FCM 토큰이 재발급 되었습니다.");
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
        return ResponseEntity.ok(Map.of("description", user1.getDescription()));
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
    @PutMapping("/alarm-settings/toggle")
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
