package FeedStudy.StudyFeed.user.controller;

import FeedStudy.StudyFeed.user.dto.DescriptionRequestDto;
import FeedStudy.StudyFeed.user.dto.NickNameCheckResponse;
import FeedStudy.StudyFeed.user.dto.ProfileImageUpdateDto;
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
    public ResponseEntity<Map<String, String>> makeNickName(
            @AuthenticationPrincipal(expression = "user") User user
    ) {
        String nickname = userService.makeNickName(user);
        return ResponseEntity.ok(Map.of("nickname", nickname));
    }

    @Operation(summary = "닉네임 수정")
    @PutMapping("/update_nickname")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> updateNickName(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") User user,
            @Parameter(description = "수정할 닉네임") @RequestParam String nickname
    ) {
        userService.updateNickname(user, nickname);
        return ResponseEntity.ok(Map.of("nickname", user.getNickName()));
    }

    @Operation(summary = "닉네임 제한 확인")
    @GetMapping("/limit_nickname")
    public ResponseEntity<NickNameCheckResponse> limitName(
            @Parameter(description = "확인할 닉네임") @RequestParam String nickname
    ) {
        NickNameCheckResponse resp = userService.checkNickname(nickname);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "닉네임 보유 여부 확인")
    @GetMapping("/has_nickname")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> hasNickName(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") User user
    ) {
        return ResponseEntity.ok(userService.hasNickName(user));
    }

    @Operation(summary = "Access Token 유효성 검사")
    @GetMapping("/check")
    public ResponseEntity<Map<String, String>> checkAccessToken(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") User user
    ) {
        return ResponseEntity.ok(userService.getNickname(user));
    }

    @Operation(summary = "유저 자기소개 수정")
    @PutMapping("/modify_description")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> modifyDescription(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody DescriptionRequestDto dto
    ) {
        User updatedUser = userService.modifyDescription(dto, user);
        return ResponseEntity.ok(Map.of(
                "description",
                updatedUser.getDescription() == null ? "" : updatedUser.getDescription()
        ));
    }

    @Operation(summary = "프로필 이미지 수정")
    @PostMapping("/profile-image")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> updateProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") User user,
            @ModelAttribute ProfileImageUpdateDto dto
    ) {
        User updated = userService.changeProfileImage(user, dto);
        return ResponseEntity.ok(Map.of(
                "profileImageUrl",
                updated.getImageUrl() == null ? "" : updated.getImageUrl()
        ));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") User user
    ) {
        userService.deleteUser(user);
        return ResponseEntity.noContent().build();
    }
}