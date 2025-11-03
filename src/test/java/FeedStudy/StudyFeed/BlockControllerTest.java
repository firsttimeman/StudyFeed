package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.block.controller.BlockController;
import FeedStudy.StudyFeed.block.dto.BlockSimpleDto;
import FeedStudy.StudyFeed.block.service.BlockService;
import FeedStudy.StudyFeed.global.config.SecurityConfig;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
import FeedStudy.StudyFeed.global.repository.RegionRepository;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BlockController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
public class BlockControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private RegionRepository regionRepository;
    @MockitoBean
    private BlockService blockService;


    private UsernamePasswordAuthenticationToken authWith(UserRole role) {
        User principal = new User();
        ReflectionTestUtils.setField(principal, "id", 1L);
        principal.setUserRole(role);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }


    @Test
    @DisplayName("유저 차단 성공(POST /api/block/{otherId})")
    void blockUser() throws Exception {
        var auth = authWith(UserRole.USER);

        mockMvc.perform(post("/api/block/{otherId}", 2L)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        Mockito.verify(blockService).createBlock(any(User.class), eq(2L));
    }


    @Test
    @DisplayName("유저 차단 해제 성공(DELETE /api/block/{otherId})")
    void unblockUser() throws Exception {
        var auth = authWith(UserRole.USER);

        mockMvc.perform(delete("/api/block/{otherId}", 3L)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().string("사용자 차단을 해제했습니다."));

        Mockito.verify(blockService).removeBlock(any(User.class), eq(3L));
    }

    @Test
    @DisplayName("차단 목록 조회 성공(GET /api/block)")
    void getBlocks() throws Exception {
        var auth = authWith(UserRole.USER);

        Mockito.when(blockService.blockList(any(User.class)))
                .thenReturn(List.<BlockSimpleDto>of());

        mockMvc.perform(get("/api/block")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        Mockito.verify(blockService).blockList(any(User.class));
    }

}
