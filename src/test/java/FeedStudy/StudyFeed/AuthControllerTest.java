package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.auth.controller.AuthController;
import FeedStudy.StudyFeed.auth.dto.SignUpRequestDto;
import FeedStudy.StudyFeed.auth.service.AuthService;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.repository.RegionRepository;
import FeedStudy.StudyFeed.user.dto.CheckAuthCodeDto;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 비활성화
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RegionRepository regionRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일 인증 요청 성공")
    void sendVerifyMail() throws Exception {
        mockMvc.perform(post("/api/auth/verifymail")
                .param("email", "test@test.com"))
                .andExpect(status().isOk());
        Mockito.verify(authService).sendVerifyMail("test@test.com");
    }

    @Test // 처음해보는식
    @DisplayName("인증 코드 검증 성공")
    void checkAuthCode() throws Exception {
        CheckAuthCodeDto dto = new CheckAuthCodeDto("test@example.com", "123456");

        mockMvc.perform(post("/api/auth/check_auth_code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(authService).checkAuthCode(any(CheckAuthCodeDto.class));

        ArgumentCaptor<CheckAuthCodeDto> captor = ArgumentCaptor.forClass(CheckAuthCodeDto.class);
        Mockito.verify(authService).checkAuthCode(captor.capture());
        CheckAuthCodeDto captorDto = captor.getValue();

        assertEquals("test@example.com", captorDto.getEmail());
        assertEquals("123456", captorDto.getCode());
    }


    @Test
    @DisplayName("회원가입 성공시 토큰 반환")
    void signUp() throws Exception {
        SignUpRequestDto dto = new SignUpRequestDto(
                "test@example.com",
                "GOOGLE",
                "123456789",
                "SKT",
                "MALE",
                LocalDate.of(1998, 5, 12),
                "Y",
                "AUTH1234"
        );

        Mockito.when(authService.login(any(String.class), any(String.class), any(String.class)))
                .thenReturn(Map.of("accessToken", "dummyAccessToken",
                        "refreshToken", "dummyRefreshToken"));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("dummyAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("dummyRefreshToken"));
    }


    @Test
    @DisplayName("로그인 성공시 토큰 반환")
    void signin() throws Exception {
        Mockito.when(authService.login(any(), any(), any()))
                .thenReturn(Map.of("accessToken", "dummyAccess", "refreshToken", "dummyRefresh"));

        mockMvc.perform(post("/api/auth/signin")
                .param("email", "test@example.com")
                .param("providerType", "google")
                .param("providerId", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("dummyAccess"))
                .andExpect(jsonPath("$.refreshToken").value("dummyRefresh"));
    }


    @Test
    @DisplayName("리프레쉬 토큰 재발급 성공")
    void refreshToken() throws Exception {
        Mockito.when(authService.refreshToken(any()))
                .thenReturn(Map.of("accessToken", "new-access", "refreshToken", "new-refresh"));

        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer refresh-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));

    }


    @Test
    @DisplayName("리프레쉬 토큰 헤더가 없을 경우 401 반환")
    void refreshToken_missing_header() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Refresh token missing"));
    }
}
