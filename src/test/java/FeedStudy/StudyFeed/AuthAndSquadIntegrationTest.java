package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.auth.service.AuthCodeService;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthAndSquadIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AuthCodeService authCodeService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Map<String, String> tokenMap = new HashMap<>();

//    @BeforeEach
    @Test
    void setUpUsers() throws Exception {
        tokenMap.clear();

        for (int i = 1; i <= 20; i++) {
            String email = "test@" + i + "test.com";
            String snsType = "kakao";
            String snsId = "kakao" + i;
            String rawPassord = snsType + snsId;

            String gender = (i % 2 == 0) ? "MALE" : "FEMALE";

            int birthYear = 1950 + (i % 21);
            String birth = birthYear + "-01-01";

            String sendEmailJsonBody = String.format("""
                    {
                    "email": "%s",
                    "providerType": "%s",
                    "providerId": "%s",
                    "name": "Tester%d",
                    "telecom": "SKT",
                    "phoneNumber": "010-1234-%04d",
                    "gender": "%s",
                    "birthDate": "%s",
                    "receiveEvent": "Y" 
                    }
                    """, email, snsType, snsId, i, i, gender, birth);

            mockMvc.perform(post("/api/auth/verifymail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sendEmailJsonBody))
                    .andExpect(status().isOk());



            //이메일 인증 과정 구현해야함 과제
            String authCode = authCodeService.getAuthCode(email);

            String signUpJsonBody = String.format("""
                    {
                        "email": "%s",
                        "providerType": "%s",
                        "providerId": "%s",
                        "name": "Tester%d",
                        "telecom": "SKT",
                        "phoneNumber": "010-1234-%04d",
                        "gender": "%s",
                        "birthDate": "%s",
                        "receiveEvent": "Y",
                        "authcode": "%s"
                    }
                    """, email, snsType, snsId, i, i, gender, birth, authCode);

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(signUpJsonBody))
                    .andExpect(status().isOk());


            // 이메일 인증후 회원 로그인 파트

            MvcResult resultSignin = mockMvc.perform(post("/api/auth/signin")
                    .param("email", email)
                    .param("snsType", snsType)
                    .param("snsId", snsId))
                    .andExpect(status().isOk())
                    .andReturn();

            String repsonse = resultSignin.getResponse().getContentAsString();
            String accessToken = JsonPath.read(repsonse, "$.accessToken");

            tokenMap.put(email, "Bearer " + accessToken);
        }

        assertEquals(20, tokenMap.size());
    }


}
