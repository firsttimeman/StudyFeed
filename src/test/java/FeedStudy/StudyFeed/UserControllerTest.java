package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.global.config.SecurityConfig;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.repository.RegionRepository;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.user.controller.UserController;
import FeedStudy.StudyFeed.user.dto.NickNameCheckResponse;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.user.service.CustomUserDetailsService;
import FeedStudy.StudyFeed.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RegionRepository regionRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    private UsernamePasswordAuthenticationToken authWithDomainUser(Long id, String nickname) {
        User principal = new User();
        principal.setNickName(nickname);
        principal.setUserRole(UserRole.USER);
        ReflectionTestUtils.setField(principal, "id", id);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

//    @Test
//    @DisplayName("랜덤 닉네임 생성 성공")
//    void makeNickname() throws Exception {
//        mockMvc.perform(get("/api/user/generate_nickname")
//                .with(user("dummyUser").roles("USER")))
//                .andExpect(status().isOk())
//                .andExpect(content().string("NickName 생성 완료"));
//
//        Mockito.verify(userService).makeNickName();
//
//    }

    @Test
    @DisplayName("닉네임 수정 성공")
    void updateNickname() throws Exception {

        User principal = new User();
        principal.setNickName("old");
        principal.setUserRole(UserRole.USER);
        ReflectionTestUtils.setField(principal, "id", 1L);

        var auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(put("/api/user/update_nickname")
                        .param("nickname", "새닉네임")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().string("NickName 적용 완료"));

        Mockito.verify(userService).updateNickname(any(User.class), eq("새닉네임"));
    }


    @Test
    @DisplayName("닉네임 제한 성공")
    void limitNickName() throws Exception {
        String nickName = "test nick";

        NickNameCheckResponse mockResponse = NickNameCheckResponse.builder()
                .valid(true)
                .available(true)
                .message("사용 가능한 닉네임입니다.")
                .normalized("테스트닉네임")
                .build();

        Mockito.when(userService.checkNickname(nickName)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/user/limit_nickname")
                        .with(user("dummy").roles("USER"))
                        .param("nickname", nickName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다."))
                .andExpect(jsonPath("$.normalized").value("테스트닉네임"));
        Mockito.verify(userService).checkNickname(nickName);

    }

    @Test
    @DisplayName("닉네임 보유 여부 확인 성공")
    void hasNickname() throws Exception {

        User principal = new User();
        principal.setNickName("anything");
        principal.setUserRole(UserRole.USER);
        ReflectionTestUtils.setField(principal, "id", 1L);

        var auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());



        Mockito.when(userService.hasNickName(any(User.class))).thenReturn(true);

        mockMvc.perform(get("/api/user/has_nickname")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Mockito.verify(userService).hasNickName(any(User.class));
    }


    @Test
    @DisplayName("FCM 토큰 재발급 성공(/update_fcm)")
    void updateFcmToken() throws Exception {
        var auth = authWithDomainUser(1L, "tester");

        String body = """
          {"fcmToken":"new-fcm-token-123"}
        """;

        mockMvc.perform(put("/api/user/update_fcm")
                        .with(authentication(auth))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("FCM 토큰이 재발급 되었습니다."));

        Mockito.verify(userService).fcmTokenRefresh(any(User.class), eq("new-fcm-token-123"));
    }

    @Test
    @DisplayName("유저 자기소개 수정 성공(/modify_description)")
    void modifyDescription() throws Exception {
        var auth = authWithDomainUser(1L, "tester");

        // 서비스가 수정된 User 반환한다고 가정
        User updated = new User();
        ReflectionTestUtils.setField(updated, "id", 1L);
        updated.setUserRole(UserRole.USER);
        updated.setNickName("tester");
        updated.setDescription("안녕하세요!");

        Mockito.when(userService.modifyDescription(any(), any(User.class)))
                .thenReturn(updated);

        String body = """
          {"description":"안녕하세요!"}
        """;

        mockMvc.perform(put("/api/user/modify_description")
                        .with(authentication(auth))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("안녕하세요!"));

        Mockito.verify(userService).modifyDescription(any(), any(User.class));
    }


    @Test
    @DisplayName("프로필 이미지 수정 성공(/profile-image)")
    void updateProfileImage() throws Exception {
        var auth = authWithDomainUser(1L, "tester");

        // 서비스가 이미지 URL 반영된 User 반환
        User updated = new User();
        ReflectionTestUtils.setField(updated, "id", 1L);
        updated.setUserRole(UserRole.USER);
        updated.setNickName("tester");
        updated.setImageUrl("https://cdn.example.com/img/abc.png");

        Mockito.when(userService.changeProfileImage(any(User.class), any()))
                .thenReturn(updated);


        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "pngbytes".getBytes()
        );

        mockMvc.perform(multipart("/api/user/profile-image")
                        .file(file)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").value("https://cdn.example.com/img/abc.png"));

        Mockito.verify(userService).changeProfileImage(any(User.class), any());
    }

    @Test
    @DisplayName("알림 전체 설정 토글 성공(/alarm-settings/toggle)")
    void toggleAlarm() throws Exception {

        var auth = authWithDomainUser(1L, "tester");


        // 서비스가 반환할 더미 User
        User updated = new User();
        ReflectionTestUtils.setField(updated, "id", 1L);
        updated.setFeedAlarm(true);
        updated.setFeedLikeAlarm(true);
        updated.setSquadChatAlarm(true);
        updated.setChatroomAlarm(true);
        updated.setSquadNotifyAlarm(true);
        updated.setUserRole(UserRole.USER);

        Mockito.when(userService.toggleAllAlarm(any(User.class), eq(true)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/user/alarm-settings/toggle")
                        .with(authentication(auth))
                        .param("enabled", "true"))
                .andExpect(status().isOk());

        Mockito.verify(userService).toggleAllAlarm(any(User.class), eq(true));
    }

}
