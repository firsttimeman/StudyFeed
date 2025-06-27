package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.auth.service.AuthCodeService;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
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

    @Autowired
    protected SquadRepository squadRepository;

    @Autowired
    protected SquadMemberRepository squadMemberRepository;

    @BeforeEach
//    @Test
    void setUpUsers() throws Exception {
        tokenMap.clear();

        for (int i = 1; i <= 5; i++) {
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
            System.out.println("✅authcode: " + authCode);

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
            System.out.println("✅ accessToken: " + accessToken);

            tokenMap.put(email, "Bearer " + accessToken);
        }


    }

    // 성공케이스
    @Test
    void CreateSquad_success_whenValidRequest() throws Exception {
        createSquadMin50Max100AllDirectDefault(1)
                .andExpect(status().isOk())
                .andExpect(content().string("Squad created"));
    }

    // 성공 케이스 시간 구체적이지 않을때
    @Test
    void CreateSquad_success_whenTimeNotSpecified() throws Exception {

        String token = tokenMap.get("test@1test.com");

        String squadJson = """
                {
                    "title": "자바 스터디",
                    "category": "프로그래밍",
                    "regionMain": "서울",
                    "regionSub": "강남구",
                    "description": "매주 토요일 자바 스터디 합니다.",
                    "minAge": 20,
                    "maxAge": 35,
                    "date": "2025-07-01",
                    "time": null,
                    "timeSpecified": false,
                    "genderRequirement": "ALL",
                    "joinType": "DIRECT",
                    "maxParticipants": 10
                }
                """;

        mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(squadJson)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Squad created"));

    }

    //유효성 검증 실패 (title date) 등등
    @ParameterizedTest
    @MethodSource("invalidSquadJsonProvider")
    void CreateSquad_failed_whenInvalidInput(String invalidJson) throws Exception {
        String token = tokenMap.get("test@1test.com");

        mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    static Stream<String> invalidSquadJsonProvider() {
        return Stream.of(
                // title 누락
                """
                        {
                            "title": null,
                            "category": "프로그래밍",
                            "regionMain": "서울",
                            "regionSub": "강남구",
                            "description": "스터디입니다.",
                            "minAge": 20,
                            "maxAge": 35,
                            "date": "2025-07-01",
                            "time": "18:30:00",
                            "timeSpecified": true,
                            "genderRequirement": "ALL",
                            "joinType": "DIRECT",
                            "maxParticipants": 10
                        }
                        """,
                // category 누락
                """
                        {
                            "title": "자바 스터디",
                            "category": null,
                            "regionMain": "서울",
                            "regionSub": "강남구",
                            "description": "스터디입니다.",
                            "minAge": 20,
                            "maxAge": 35,
                            "date": "2025-07-01",
                            "time": "18:30:00",
                            "timeSpecified": true,
                            "genderRequirement": "ALL",
                            "joinType": "DIRECT",
                            "maxParticipants": 10
                        }
                        """,
                //regionMain 누락
                """
                        {
                            "title": "자바 스터디",
                            "category": "프로그래밍",
                            "regionMain": null,
                            "regionSub": "강남구",
                            "description": "스터디입니다.",
                            "minAge": 20,
                            "maxAge": 35,
                            "date": "2025-07-01",
                            "time": "18:30:00",
                            "timeSpecified": true,
                            "genderRequirement": "ALL",
                            "joinType": "DIRECT",
                            "maxParticipants": 10
                        }
                        """,

                """
                        {
                                            "title": "자바 스터디",
                                            "category": "프로그래밍",
                                            "regionMain": "서울",
                                            "regionSub": null,
                                            "description": "스터디입니다.",
                                            "minAge": 20,
                                            "maxAge": 35,
                                            "date": "2025-07-01",
                                            "time": "18:30:00",
                                            "timeSpecified": true,
                                            "genderRequirement": "ALL",
                                            "joinType": "DIRECT",
                                            "maxParticipants": 10
                                        }
                        """

        );
    }

    // 나이 범위 오류
    @ParameterizedTest
    @MethodSource("invalidAgeRangeProvider")
    void CreateSquad_failed_dueToInvalidAgeRange(int minAge, int maxAge) throws Exception {
        String token = tokenMap.get("test@1test.com");

        String squadJson = String.format("""
                {
                    "title": "자바 스터디",
                    "category": "프로그래밍",
                    "regionMain": "서울",
                    "regionSub": "강남구",
                    "description": "매주 토요일 자바 스터디 합니다.",
                    "minAge": %d,
                    "maxAge": %d,
                    "date": "2025-07-01",
                    "time": "18:30:00",
                    "timeSpecified": true,
                    "genderRequirement": "ALL",
                    "joinType": "DIRECT",
                    "maxParticipants": 10
                }
                """, minAge, maxAge);

        mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(squadJson)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }


    static Stream<Arguments> invalidAgeRangeProvider() {
        return Stream.of(
                Arguments.of(40, 30),
                Arguments.of(100, 99),
                Arguments.of(51, 50)
        );
    }


    @Test
    void CreateSquad_fail_whenNoTokenProvided() throws Exception {
        String squadJson = """
                {
                    "title": "자바 스터디",
                    "category": "프로그래밍",
                    "regionMain": "서울",
                    "regionSub": "강남구",
                    "description": "매주 토요일 자바 스터디 합니다.",
                    "minAge": 50,
                    "maxAge": 60,
                    "date": "2025-07-01",
                    "time": "18:30:00",
                    "timeSpecified": true,
                    "genderRequirement": "ALL",
                    "joinType": "DIRECT",
                    "maxParticipants": 10
                }
                """;

        mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(squadJson))
                .andExpect(status().isForbidden()); // 401
    }

    // 수정 성공 케이스
    @Test
    @DisplayName("")
    void updateSquad_success() throws Exception {
        String token = tokenMap.get("test@1test.com");
        Long squadId = createDummySquad(token);

        String updateJson = """
                    {
                        "title": "수정된 모임 제목",
                        "category": "스터디",
                        "regionMain": "서울",
                        "regionSub": "강남구",
                        "description": "설명이 수정되었습니다.",
                        "minAge": 50,
                        "maxAge": 90,
                        "date": "2025-07-25",
                        "time": "18:30",
                        "timeSpecified": true,
                        "genderRequirement": "ALL",
                        "joinType": "DIRECT",
                        "maxParticipants": 10
                    }
                """;

        mockMvc.perform(put("/api/squad/modify/{id}", squadId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());

    }

    // 수정 인증 실패 케이스
    @Test
    void updateSquad_unauthorized() throws Exception {
        String token = tokenMap.get("test@1test.com");
        Long squadId = createDummySquad(token);


        String updateJson = """
                    {
                        "title": "수정된 제목",
                        "category": "스터디",
                        "regionMain": "서울",
                        "regionSub": "강남구",
                        "description": "설명",
                        "minAge": 20,
                        "maxAge": 30,
                        "date": "2025-07-10",
                        "time": "18:00",
                        "timeSpecified": true,
                        "genderRequirement": "ALL",
                        "joinType": "DIRECT",
                        "maxParticipants": 10
                    }
                """;

        // Authorization 헤더 없이 요청
        mockMvc.perform(put("/api/squad/modify/{id}", squadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());

    }


    // 수정 권한 실패 케이스(주인)
    @Test
    void updateSquad_forbidden_notOwner() throws Exception {
        String ownerToken = tokenMap.get("test@1test.com");
        String otherToken = tokenMap.get("test@2test.com");

        Long squadId = createDummySquad(ownerToken);

        String updateJson = """
        {
            "title": "권한 없음 수정",
            "category": "스터디",
            "regionMain": "서울",
            "regionSub": "강남구",
            "description": "권한 없음 설명",
            "minAge": 50,
            "maxAge": 80,
            "date": "2025-07-12",
            "time": "19:00",
            "timeSpecified": true,
            "genderRequirement": "ALL",
            "joinType": "DIRECT",
            "maxParticipants": 10
        }
    """;

        mockMvc.perform(put("/api/squad/modify/{id}", squadId)
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }


    // 수정 유효성 검증 실패 title date 등등
    @Test
    void updateSquad_failed_whenRequiredFieldsMissing() throws Exception {
        String token = tokenMap.get("test@1test.com");
        Long squadId = createDummySquad(token);

        String invalidUpdateJson = """
    {
        "title": "유효성 테스트",
        "category": null,
        "regionMain": null,
        "regionSub": null,
        "description": "빠진 필드 테스트입니다.",
        "minAge": 50,
        "maxAge": 90,
        "date": "2025-07-25",
        "time": "18:30",
        "timeSpecified": true,
        "genderRequirement": "ALL",
        "joinType": "DIRECT",
        "maxParticipants": 10
    }
    """;

        mockMvc.perform(put("/api/squad/modify/{id}", squadId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUpdateJson))
                .andExpect(status().isBadRequest());
    }

    private Long createDummySquad(String token) throws Exception {

        String createJson = """
                {
                    "title": "기존 모임 제목",
                    "category": "스터디",
                    "regionMain": "서울",
                    "regionSub": "강남구",
                    "description": "원래 설명입니다.",
                    "minAge": 50,
                    "maxAge": 90,
                    "date": "2025-07-10",
                    "time": "18:00",
                    "timeSpecified": true,
                    "genderRequirement": "ALL",
                    "joinType": "DIRECT",
                    "maxParticipants": 10
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();


        String content = result.getResponse().getContentAsString();
        Number idNumber = JsonPath.read(content, "$.id");
        Long squadId = idNumber.longValue();

        return squadId;
    }


    //delete squad 정상 삭제
    @Test
    void deleteSquad_success_whenOwnerAndForcedDelete() throws Exception {
        String token = tokenMap.get("test@1test.com");
        Long squadId = createDummySquad(token); // squad + 자기 자신 포함된 상태

        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", token)
                        .param("isForcedDelete", "true")
                )
                .andExpect(status().isOk());
    }


    //delete squad 소유자가 아닌 유저
    @Test
    void deleteSquad_fail_whenNotOwner() throws Exception {
        String ownerToken = tokenMap.get("test@1test.com");
        String otherToken = tokenMap.get("test@2test.com");

        Long squadId = createDummySquad(ownerToken); // 모임은 test@1test.com 이 소유자

        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", otherToken)
                        .param("isForcedDelete", "true"))
                .andExpect(status().isForbidden()); // 403
    }

    //delete squad 멤버가 남아있는데 강제 삭제가 false
    @Test
    void deleteSquad_fail_whenMembersRemainAndForcedDeleteFalse() throws Exception {
        String token = tokenMap.get("test@1test.com");

        Long squadId = createDummySquad(token); // 자기 자신이 멤버로 포함됨

        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", token)
                        .param("isForcedDelete", "false"))
                .andExpect(status().isBadRequest()); // 또는 적절한 errorCode 따라 조정
    }

    //delete squad 멤버가 남아있는데 강제 삭제가 true
    @Test
    void deleteSquad_success_whenMembersRemainAndForcedDeleteTrue() throws Exception {
        String token = tokenMap.get("test@1test.com");

        Long squadId = createDummySquad(token);

        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", token)
                        .param("isForcedDelete", "true"))
                .andExpect(status().isOk());
    }


    // mysquad 정상
    @Test
    void getMySquads_success() throws Exception {
        String token = tokenMap.get("test@1test.com");

        createDummySquad(token);
        createDummySquad(token);

        MvcResult result = mockMvc.perform(get("/api/squad/mine")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andReturn();
    }




    // mysquad 비로그인 호출
    @Test
    void getMySquads_unauthorized_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/squad/mine")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }


    // mysquad 모임이 없을때 정상 호출
    @Test
    void getMySquads_success_whenNoSquads() throws Exception {
        String token = tokenMap.get("test@1test.com");

        mockMvc.perform(get("/api/squad/mine")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }




    // 이미 조인인 상태인 경우
    @Test
    void joinOrGetToken_success_whenJoined() throws Exception {
        String token = tokenMap.get("test@1test.com");
        Long squadId = createDummySquad(token); // 생성자=JOINED

        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("joined"))
                .andExpect(jsonPath("$.chatToken").exists());
    }

    //대기 상태일경우
    @Test
    void joinOrGetToken_success_whenPending() throws Exception {
        String ownerToken = tokenMap.get("test@1test.com");
        String userToken = tokenMap.get("test@2test.com");

        Long squadId = createApprovalSquad(ownerToken); // 승인 방식 모임 생성

        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("requested"));
    }




    private Long createApprovalSquad(String token) throws Exception {
        String createJson = """
        {
            "title": "승인 모임",
            "category": "스터디",
            "regionMain": "서울",
            "regionSub": "강남구",
            "description": "설명입니다.",
            "minAge": 50,
            "maxAge": 90,
            "date": "2025-07-10",
            "time": "18:00",
            "timeSpecified": true,
            "genderRequirement": "ALL",
            "joinType": "APPROVAL",
            "maxParticipants": 10
        }
        """;

        MvcResult result = mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Number idNumber = JsonPath.read(content, "$.id");
        return idNumber.longValue();
    }

    @Test
    void joinOrGetToken_success_whenAlreadyPending() throws Exception {
        String ownerToken = tokenMap.get("test@1test.com");
        String userToken = tokenMap.get("test@2test.com");

        Long squadId = createApprovalSquad(ownerToken);

        // 첫 번째 요청 → requested
        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("requested"));

        // 두 번째 요청 → pending
        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"));


    }

    @Test
    void joinOrGetToken_fail_whenStatusRejected() throws Exception {
        String ownerToken = tokenMap.get("test@1test.com");
        String userToken = tokenMap.get("test@4test.com");

        Long squadId = createApprovalSquad(ownerToken);

        // REJECTED 상태로 수동 세팅
        User user = userRepository.findByEmail("test@4test.com").orElseThrow();
        Squad squad = squadRepository.findById(squadId).orElseThrow();
        SquadMember squadmember = SquadMember.create(user, squad);
        squadmember.setAttendanceStatus(AttendanceStatus.REJECTED);
        squadMemberRepository.save(squadmember);

        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void joinOrGetToken_fail_whenSquadNotFound() throws Exception {
        String token = tokenMap.get("test@1test.com");

        mockMvc.perform(post("/api/squad/999999/join-or-token")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void joinOrGetToken_fail_whenKickedOut() throws Exception {
        String ownerToken = tokenMap.get("test@1test.com");
        String kickedToken = tokenMap.get("test@5test.com");

        Long squadId = createApprovalSquad(ownerToken);

        User user = userRepository.findByEmail("test@5test.com").orElseThrow();
        Squad squad = squadRepository.findById(squadId).orElseThrow();

        SquadMember kicked = SquadMember.create(user, squad);
        kicked.setAttendanceStatus(AttendanceStatus.KICKED_OUT);
        squadMemberRepository.save(kicked);

        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", kickedToken))
                .andExpect(status().isForbidden());
    }

    @Test // 여기부터 다시
    void homeSquad_success_basicFilter() throws Exception {
        String token = tokenMap.get("test@1test.com");
        createSquad(token, "운동", "서울", "강남구");

        mockMvc.perform(get("/api/squad/home")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list[0].category").value("운동"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    public String getToken(int id) {
        return tokenMap.get("user" + id + "@test.com");
    }

    public ResultActions createSquadDefault(int id, int minAge, int maxAge, Gender gender, JoinType joinType)
            throws Exception {
        return createSquad(id, "자바 스터디", "프로그래밍", "매주 토요일 자바 스터디 합니다.",
                "서울", "강남구",
                LocalDate.now().plusDays(3), LocalTime.of(10, 0, 0),
                gender, minAge, maxAge, true, joinType, 5);
    }

    public ResultActions createSquadMin50Max100AllApprovalDefault(int userId) throws Exception {
        return createSquadDefault(userId, 50, 100, Gender.ALL, JoinType.APPROVAL);
    }

    public ResultActions createSquadMin50Max100AllDirectDefault(int userId) throws Exception {
        return createSquadDefault(userId, 50, 100, Gender.ALL, JoinType.DIRECT);
    }

    private ResultActions createSquad(int userId, String title, String category, String description, String regionMain,
                                      String regionSub, LocalDate date, LocalTime time, Gender gender, int minAge,
                                      int maxAge, boolean timeSpecified, JoinType joinType, int maxParticipants)
            throws Exception {
        String token = getToken(userId);

        String body = """
    {
        "title": "%s",
        "category": "%s",
        "regionMain": "%s",
        "regionSub": "%s",
        "description": "%s",
        "minAge": %d,
        "maxAge": %d,
        "date": %s,
        "time": %s,
        "timeSpecified": %s,
        "genderRequirement": %s,
        "joinType": %s,
        "maxParticipants": %d
    }
    """.formatted(title, category, regionMain, regionSub, description, minAge, maxAge, date, time, String.valueOf(timeSpecified), gender, joinType, maxParticipants);

        return mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", token));
    }

    public ResultActions joinOrGetToken(long squadPid, int userId) throws Exception {
        String token = getToken(userId);
        return mockMvc.perform(post("/api/squad/" + squadPid + "/join-or-token")
                .header("Authorization", token));
    }

    public ResultActions deleteSquad(long squadPid, int creatorId, boolean isForced) throws Exception {
        String token = getToken(creatorId);
        return mockMvc.perform(delete("/api/squad/delete/" + squadPid)
                .header("Authorization", token)
                .param("isForcedDelete", String.valueOf(isForced)));
    }

    private void createSquad(String token, String category, String regionMain, String regionSub) throws Exception {
        String body = """
    {
        "title": "모임 제목",
        "category": "%s",
        "regionMain": "%s",
        "regionSub": "%s",
        "description": "설명입니다.",
        "minAge": 50,
        "maxAge": 80,
        "date": "2025-07-01",
        "time": "18:00",
        "timeSpecified": true,
        "genderRequirement": "ALL",
        "joinType": "DIRECT",
        "maxParticipants": 10
    }
    """.formatted(category, regionMain, regionSub);

        mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

}