package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.auth.service.AuthCodeService;
import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.global.repository.RegionRepository;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.Gender;
import FeedStudy.StudyFeed.global.type.JoinType;
import FeedStudy.StudyFeed.global.type.UserRole;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertFalseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Autowired
    protected RegionRepository regionRepository;
    @Autowired
    private BlockRepository blockRepository;


    @BeforeEach
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

    @BeforeEach
    void clearDB() {
        squadRepository.deleteAll();
    }

    // 성공케이스 Direct
    @Test
    @DisplayName("스쿼드 신청시 바로 들어가지는 경우 DIRECT")
    void CreateSquad_success_whenValidRequest_Direct() throws Exception {
        createSquadMin50Max100AllDirectDefault(1)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("스쿼드 신청시 바로 들어가지는 경우 Approval")
    void CreateSquad_success_whenValidRequest_Approval() throws Exception {
        createSquadMin50Max100AllApprovalDefault(1)
        .andExpect(status().isOk());
    }


    // 성공 케이스 시간 구체적이지 않을때
    @Test
    @DisplayName("스쿼드 개설시 시간을 구체적으로 적지 않았을때")
    void CreateSquad_success_whenTimeNotSpecified() throws Exception {

        createSquadMin50Max100AllDirectNoTime(1)
                .andExpect(status().isOk());

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
    @Test
    void CreateSquad_failed_dueToInvalidAgeRange() throws Exception {

        createSquadMin70Max50AllDirect(1)
                .andExpect(status().isBadRequest());
    }



    @Test
    void CreateSquad_fail_whenNoTokenProvided() throws Exception {
        createSquadMin50Max100AllDirectDefaultNoToken()
                .andExpect(status().isForbidden()); // 401
    }

    // 수정 성공 케이스
    @Test
    @DisplayName("스쿼드 수정 성공")
    void updateSquad_success() throws Exception {
        Long squadId = createDummySquadAndReturnSquadId(1);
        updateSquadMin50Max70DirectDefault(1,squadId)
                .andExpect(status().isOk());


    }

   //  수정 인증 실패 케이스
    @Test
    void updateSquad_unauthorized() throws Exception {
        Long squadId = createDummySquadAndReturnSquadId(1);


        // Authorization 헤더 없이 요청
        updateSquadMin50Max70DirectNoToken(1,squadId)
                .andExpect(status().isForbidden());

    }


//     수정 권한 실패 케이스(주인)
    @Test
    void updateSquad_forbidden_notOwner() throws Exception {

        Long squadId = createDummySquadAndReturnSquadId(1);

        updateSquadMin50Max70DirectDefault(2,squadId)
                .andExpect(status().isForbidden());

    }


    // 수정 유효성 검증 실패 title date 등등
    @Test
    void updateSquad_failed_whenRequiredFieldsMissing() throws Exception {
        Long squadId = createDummySquadAndReturnSquadId(1);


        updateSquadMin50Max70DirectInvalid(1, squadId)
                .andExpect(status().isBadRequest());
    }


//delete squad 정상 삭제
    @Test
    void deleteSquad_success_whenOwnerAndForcedDelete() throws Exception {

        Long squadId = createDummySquadAndReturnSquadId(1);

        String token = getToken(1);


        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", token)
                        .param("isForcedDelete", "true")
                )
                .andExpect(status().isOk());
    }

    //delete squad 소유자가 아닌 유저
    @Test
    void deleteSquad_fail_whenNotOwner() throws Exception {

        String other = getToken(2);


        Long squadId = createDummySquadAndReturnSquadId(1); // 모임은 test@1test.com 이 소유자

        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", other)
                        .param("isForcedDelete", "true"))
                .andExpect(status().isForbidden()); // 403
    }



    //delete squad 멤버가 남아있는데 강제 삭제가 false
    @Test
    void deleteSquad_fail_whenMembersRemainAndForcedDeleteFalse() throws Exception {
        String token = getToken(1);
        Long squadId = createDummySquadAndReturnSquadId(1);

        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", token)
                        .param("isForcedDelete", "false"))
                .andExpect(status().isBadRequest()); // 또는 적절한 errorCode 따라 조정
    }

    //delete squad 멤버가 남아있는데 강제 삭제가 true
    @Test
    void deleteSquad_success_whenMembersRemainAndForcedDeleteTrue() throws Exception {
        String token = getToken(1);

        Long squadId = createDummySquadAndReturnSquadId(1);

        mockMvc.perform(delete("/api/squad/delete/{id}", squadId)
                        .header("Authorization", token)
                        .param("isForcedDelete", "true"))
                .andExpect(status().isOk());
    }


    // mysquad 정상
    @Test
    void getMySquads_success() throws Exception {

        createSquadDefault(1, 50, 70, Gender.ALL, JoinType.DIRECT);
        createSquadDefault(1, 50, 70, Gender.ALL, JoinType.DIRECT);

        String token = getToken(1);


       mockMvc.perform(get("/api/squad/mine")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }
//



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

        String token = getToken(1);

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

        String token = getToken(1);
        Long squadId = createDummySquadAndReturnSquadId(1); // 생성자=JOINED

        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("joined"))
                .andExpect(jsonPath("$.chatToken").exists());
    }

    //대기 상태일경우
    @Test
    void joinOrGetToken_success_whenPending() throws Exception {


        String userToken = getToken(2);

        Long squadId = createDummySquad_Approval_AndReturnSquadId(1); // 승인 방식 모임 생성

        mockMvc.perform(post("/api/squad/" + squadId + "/join-or-token")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("requested"));
    }





    @Test
    void joinOrGetToken_success_whenAlreadyPending() throws Exception {
//        String ownerToken = tokenMap.get("test@1test.com");
//        String userToken = tokenMap.get("test@2test.com");


        Long squadId = createDummySquad_Approval_AndReturnSquadId(1);
        String userToken = getToken(2);

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
        String userToken = tokenMap.get("test@4test.com");

        Long squadId = createDummySquad_Approval_AndReturnSquadId(1);

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
        String token = getToken(1);
        createDummySquadAndReturnSquadId(1);

        mockMvc.perform(post("/api/squad/999999/join-or-token")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void joinOrGetToken_fail_whenKickedOut() throws Exception {
        String kickedToken = tokenMap.get("test@5test.com");

        Long squadId = createDummySquad_Approval_AndReturnSquadId(1);

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
        createDummySquadAndReturnSquadId(1);

        mockMvc.perform(get("/api/squad/home")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list[0].category").value("스터디"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void homeSquad_unauthorized_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/squad/home"))
                .andExpect(status().isForbidden());
    }

    @Test
    void homeSquad_success_filterByRecruitingOnly() throws Exception {
        String token = getToken(1);
        createDummySquadAndReturnSquadId(1);

        mockMvc.perform(get("/api/squad/home")
                .header("Authorization", token)
                .param("recruitingOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].category").value("스터디"));
    }

    @Test
    void homeSquad_success_filterByCategoryAndRegion() throws Exception {
        String token = getToken(1);
        createDummySquadAndReturnSquadId(1);

        mockMvc.perform(get("/api/squad/home")
                .header("Authorization", token)
                .param("category", "스터디")
                .param("regionMain", "서울")
                .param("regionSub", "강남구"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(1));


    }

    @Test
    void homeSquad_success_pagination() throws Exception {
        String token = getToken(1);

        for (int i = 0; i < 15; i++) {
            createSquadDefault(1, 50, 70, Gender.ALL, JoinType.DIRECT);
        }

        System.out.println("squad count : " + squadRepository.count());

        mockMvc.perform(get("/api/squad/home")
                        .header("Authorization", token)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(5))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void homeSquad_excludeBlockedUserSquads() throws Exception {

        User userA = userRepository.findByEmail("test@1test.com").orElseThrow();
        User userB = userRepository.findByEmail("test@2test.com").orElseThrow();
        blockRepository.save(new Block(userA, userB));

        createSquadMin50Max100AllDirectDefault(2); // B가 만든 스쿼드

        createSquadMin50Max100AllDirectDefault(1); // A가 만든 스쿼드

        String content = mockMvc.perform(get("/api/squad/home")
                        .header("Authorization", tokenMap.get("test@1test.com"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(content.contains("test@2test.com"));
    }

    @Test
    void getParticipants_success() throws Exception {
        Long squadId = createDummySquad_Approval_AndReturnSquadId(1);

        addPendingSquadMember("test@2test.com", squadId);
        String token = getToken(1);

        MvcResult result = mockMvc.perform(get("/api/squad/{id}/participants", squadId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void getParticipants_fail_whenJoinTypeIsDirect() throws Exception {
        Long squadId = createDummySquadAndReturnSquadId(1); // JoinType.DIRECT
        String token = getToken(1); // owner

        mockMvc.perform(get("/api/squad/{id}/participants", squadId)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("NOT_APPROVAL_SQUAD"));
    }

    @Test
    void approveParticipant_success() throws Exception {
        Long squadId = createDummySquad_Approval_AndReturnSquadId(1);
        addPendingSquadMember("test@2test.com", squadId);

        String token = getToken(1);

        mockMvc.perform(put("/api/squad/{squadId}/approve/{userId}", squadId, getUserIdByEmail("test@2test.com"))
                        .header("Authorization", token))
                .andExpect(status().isOk());

        Squad squad = squadRepository.findById(squadId).orElseThrow();
        User member = userRepository.findByEmail("test@2test.com").orElseThrow();

        AttendanceStatus status = squad.getMembers().stream()
                .filter(m -> m.getUser().equals(member))
                .map(SquadMember::getAttendanceStatus)
                .findAny().orElseThrow();

        assertThat(status).isEqualTo(AttendanceStatus.JOINED);

    }

    @Test
    void approveParticipant_fail_squadFull() throws Exception {
        Long squadId = createDummySquad_Approval_AndReturnSquadId(1);
        fillSquadWithJoinedMembers(squadId); // maxParticipants만큼 이미 승인 상태

        addPendingSquadMember("test@4test.com", squadId);
        String token = getToken(1);

        mockMvc.perform(put("/api/squad/{squadId}/approve/{userId}", squadId, getUserIdByEmail("test@4test.com"))
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("SQUAD_FULL"));
    }

    @Test
    void rejectParticipant_success() throws Exception {
        Long squadId = createDummySquad_Approval_AndReturnSquadId(1); // owner = test@1
        addPendingSquadMember("test@2test.com", squadId);
        String token = getToken(1); // 주최자

        mockMvc.perform(put("/api/squad/{squadId}/reject/{userId}", squadId, getUserIdByEmail("test@2test.com"))
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 검증
        Squad squad = squadRepository.findById(squadId).orElseThrow();
        User member = userRepository.findByEmail("test@2test.com").orElseThrow();
        AttendanceStatus status = squad.getMembers().stream()
                .filter(m -> m.getUser().equals(member))
                .map(SquadMember::getAttendanceStatus)
                .findFirst().orElseThrow();

        assertThat(status).isEqualTo(AttendanceStatus.REJECTED);
    }

    @Test
    void rejectParticipant_fail_memberNotFound() throws Exception {
        Long squadId = createDummySquad_Approval_AndReturnSquadId(1);
        String token = getToken(1); // 주최자

        mockMvc.perform(put("/api/squad/{squadId}/reject/{userId}", squadId, getUserIdByEmail("test@4test.com"))
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SQUAD_MEMBER_NOT_FOUND"));
    }


    @Test
    void kickOffParticipant_success() throws Exception {
        Long squadId = createDummySquad_Approval_AndReturnSquadId(1); // owner = test@1
        addJoinedSquadMember("test@2test.com", squadId); // test@2 참가

        String token = getToken(1); // owner token

        mockMvc.perform(put("/api/squad/{squadId}/kick/{userId}", squadId, getUserIdByEmail("test@2test.com"))
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 검증
        Squad squad = squadRepository.findById(squadId).orElseThrow();
        User user = userRepository.findByEmail("test@2test.com").orElseThrow();

        SquadMember member = squad.getMembers().stream()
                .filter(m -> m.getUser().equals(user))
                .findAny().orElseThrow();

        assertThat(member.getAttendanceStatus()).isEqualTo(AttendanceStatus.KICKED_OUT);
    }

    @Test
    void kickOffParticipant_fail_memberNotFound() throws Exception {
        Long squadId = createDummySquad_Approval_AndReturnSquadId(1); // owner = test@1
        String token = getToken(1); // owner

        mockMvc.perform(put("/api/squad/{squadId}/kick/{userId}", squadId, getUserIdByEmail("test@5test.com"))
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SQUAD_MEMBER_NOT_FOUND")); // 예외 처리 필요
    }


    private void addJoinedSquadMember(String email, Long squadId) {
        createUserIfNotExists(email);
        Squad squad = squadRepository.findById(squadId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();

        SquadMember sm = new SquadMember();
        sm.setUser(user);
        sm.setSquad(squad);
        sm.setAttendanceStatus(AttendanceStatus.JOINED);

        squad.getMembers().add(sm);
        squadMemberRepository.save(sm);
    }



    private void fillSquadWithJoinedMembers(Long squadId) {
        Squad squad = squadRepository.findById(squadId).orElseThrow();
        for (int i = 10; i < 10 + squad.getMaxParticipants(); i++) {
            String email = "test@" + i + "test.com";
            createUserIfNotExists(email);
            User user = userRepository.findByEmail(email).orElseThrow();
            SquadMember sm = new SquadMember();
            sm.setUser(user);
            sm.setSquad(squad);
            sm.setAttendanceStatus(AttendanceStatus.JOINED);
            squad.getMembers().add(sm);
            squadMemberRepository.save(sm);
        }
    }

    private void createUserIfNotExists(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password"));
            user.setGender(String.valueOf(Gender.MALE));
            user.setBirthDate(LocalDate.of(1950, 1, 1));
            user.setUserRole(UserRole.USER);
            userRepository.save(user);
        }
    }

    private Long getUserIdByEmail(String mail) {
        return userRepository.findByEmail(mail)
                .orElseThrow()
                .getId();
    }


    public String getToken(int id) {
        return tokenMap.get("test@" + id + "test.com");
    }

    public ResultActions createSquadDefault(int id, int minAge, int maxAge, Gender gender, JoinType joinType)
            throws Exception {
        return createSquad(id, "자바 스터디", "프로그래밍", "매주 토요일 자바 스터디 합니다.",
                "서울", "강남구",
                LocalDate.now().plusDays(3), LocalTime.of(10, 0, 0),
                gender, minAge, maxAge, true, joinType, 5);
    }

    public ResultActions createSquadNoTokenDefault(int minAge, int maxAge, Gender gender, JoinType joinType) throws Exception {
        return createSquadNoToken("자바 스터디", "프로그래밍", "매주 토요일 자바 스터디 합니다.",
                "서울", "강남구",
                LocalDate.now().plusDays(3), LocalTime.of(10, 0, 0),
                gender, minAge, maxAge, true, joinType, 5);
    }

    public ResultActions createSquadNoTime(int id, int minAge, int maxAge, Gender gender, JoinType joinType) throws Exception {
        return createSquadNoTime(id, "자바 스터디", "프로그래밍", "매주 토요일 자바 스터디 합니다.",
                "서울", "강남구",
                LocalDate.now().plusDays(3), null, gender,
                minAge, maxAge, false, joinType, 5);
    }


    public ResultActions createSquadMin50Max100AllApprovalDefault(int userId) throws Exception {
        return createSquadDefault(userId, 50, 100, Gender.ALL, JoinType.APPROVAL);
    }

    public ResultActions createSquadMin50Max100AllDirectDefault(int userId) throws Exception {
        return createSquadDefault(userId, 50, 100, Gender.ALL, JoinType.DIRECT);
    }

    public ResultActions createSquadMin50Max100AllDirectDefaultNoToken() throws Exception {
        return createSquadNoTokenDefault(50, 100, Gender.ALL, JoinType.APPROVAL);
    }

    public ResultActions createSquadMin50Max100AllDirectNoTime(int userId) throws Exception {
        return createSquadNoTime(userId, 50, 100, Gender.ALL, JoinType.DIRECT);
    }

    public ResultActions createSquadMin70Max50AllDirect(int userId) throws Exception {
        return createSquadDefault(userId, 70, 50, Gender.ALL, JoinType.DIRECT);
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
        "date": "%s",
        "time": "%s",
        "timeSpecified": %s,
        "genderRequirement": "%s",
        "joinType": "%s",
        "maxParticipants": %d
    }
    """.formatted(title, category, regionMain, regionSub, description, minAge, maxAge, date, time, String.valueOf(timeSpecified), gender, joinType, maxParticipants);

        return mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", token));
    }

    private ResultActions createSquadNoTime(int userId, String title, String category, String description, String regionMain,
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
        "date": "%s",
        "time": %s,
        "timeSpecified": %s,
        "genderRequirement": "%s",
        "joinType": "%s",
        "maxParticipants": %d
    }
    """.formatted(title, category, regionMain, regionSub, description, minAge, maxAge, date, time, String.valueOf(timeSpecified), gender, joinType, maxParticipants);

        return mockMvc.perform(post("/api/squad/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", token));
    }

    private ResultActions createSquadNoToken(String title, String category, String description, String regionMain,
                                      String regionSub, LocalDate date, LocalTime time, Gender gender, int minAge,
                                      int maxAge, boolean timeSpecified, JoinType joinType, int maxParticipants)
            throws Exception {


        String body = """
    {
        "title": "%s",
        "category": "%s",
        "regionMain": "%s",
        "regionSub": "%s",
        "description": "%s",
        "minAge": %d,
        "maxAge": %d,
        "date": "%s",
        "time": "%s",
        "timeSpecified": %s,
        "genderRequirement": "%s",
        "joinType": "%s",
        "maxParticipants": %d
    }
    """.formatted(title, category, regionMain, regionSub, description, minAge, maxAge, date, time, String.valueOf(timeSpecified), gender, joinType, maxParticipants);

        return mockMvc.perform(post("/api/squad/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }



    private Long createDummySquadAndReturnSquadId(int id) throws Exception {

        String token = getToken(id);

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
                .andReturn();


        String content = result.getResponse().getContentAsString();
        Number idNumber = JsonPath.read(content, "$.id");
        Long squadId = idNumber.longValue();

        return squadId;
    }

    private Long createDummySquad_Approval_AndReturnSquadId(int id) throws Exception {

        String token = getToken(id);

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
                    "joinType": "APPROVAL",
                    "maxParticipants": 10
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/squad/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .header("Authorization", token))
                .andReturn();


        String content = result.getResponse().getContentAsString();
        Number idNumber = JsonPath.read(content, "$.id");
        Long squadId = idNumber.longValue();

        return squadId;
    }


    private ResultActions updateSquad(int userId, String title, String category, String description, String regionMain,
                                             String regionSub, LocalDate date, LocalTime time, Gender gender, int minAge,
                                             int maxAge, boolean timeSpecified, JoinType joinType, int maxParticipants, Long squadId) throws Exception {

        String token = getToken(userId);

        String updateJson = String.format("""
                {
                    "title": "%s",
                    "category": "%s",
                    "regionMain": "%s",
                    "regionSub": "%s",
                    "description": "%s",
                    "minAge": %d,
                    "maxAge": %d,
                    "date": "%s",
                    "time": "%s",
                    "timeSpecified": %s,
                    "genderRequirement": "%s",
                    "joinType": "%s",
                    "maxParticipants": %d
                }
            """,title, category, regionMain, regionSub, description, minAge, maxAge, date, time, timeSpecified, gender, joinType, maxParticipants);

        return mockMvc.perform(put("/api/squad/modify/{id}", squadId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson));

    }

    private ResultActions updateSquadWithoutToken(String title, String category, String description, String regionMain,
                                      String regionSub, LocalDate date, LocalTime time, Gender gender, int minAge,
                                      int maxAge, boolean timeSpecified, JoinType joinType, int maxParticipants, Long squadId) throws Exception {


        String updateJson = String.format("""
                {
                    "title": "%s",
                    "category": "%s",
                    "regionMain": "%s",
                    "regionSub": "%s",
                    "description": "%s",
                    "minAge": %d,
                    "maxAge": %d,
                    "date": "%s",
                    "time": "%s",
                    "timeSpecified": %s,
                    "genderRequirement": "%s",
                    "joinType": "%s",
                    "maxParticipants": %d
                }
            """,title, category, regionMain, regionSub, description, minAge, maxAge, date, time, timeSpecified, gender, joinType, maxParticipants);

        return mockMvc.perform(put("/api/squad/modify/{id}", squadId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson));

    }

    private ResultActions updateSquadInvalid(int userId, String title, String category, String description, String regionMain,
                                      String regionSub, LocalDate date, LocalTime time, Gender gender, int minAge,
                                      int maxAge, boolean timeSpecified, JoinType joinType, int maxParticipants, Long squadId) throws Exception {

        String token = getToken(userId);

        String updateJson = String.format("""
                {
                    "title": %s,
                    "category": %s,
                    "regionMain": "%s",
                    "regionSub": "%s",
                    "description": %s,
                    "minAge": %d,
                    "maxAge": %d,
                    "date": "%s",
                    "time": "%s",
                    "timeSpecified": %s,
                    "genderRequirement": "%s",
                    "joinType": "%s",
                    "maxParticipants": %d
                }
            """, null, null, regionMain, regionSub, null, minAge, maxAge, date, time, timeSpecified, gender, joinType, maxParticipants);

        return mockMvc.perform(put("/api/squad/modify/{id}", squadId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson));

    }


    public ResultActions updateSquadMin50Max70DirectDefault(int userId ,Long squadId) throws Exception {
        return updateSquadDefault(userId,50, 70, Gender.ALL, JoinType.DIRECT, squadId);
    }

    public ResultActions updateSquadMin50Max70DirectNoToken(int userId ,Long squadId) throws Exception {
        return updateSquadNoToken(userId,50, 70, Gender.ALL, JoinType.DIRECT, squadId);
    }

    public ResultActions updateSquadMin50Max70DirectInvalid(int userId ,Long squadId) throws Exception {
        return updateSquadInvalidTitleCategoryDescription(userId, 50, 70,
                Gender.ALL, JoinType.DIRECT, squadId);
    }



    public ResultActions updateSquadDefault(int id, int minAge, int maxAge, Gender gender, JoinType joinType, Long squadId) throws Exception {
        return updateSquad(id, "자바 스터디", "프로그래밍", "매주 토요일 자바 스터디 합니다.",
                "서울", "강남구",
                LocalDate.now().plusDays(3), LocalTime.of(10, 0, 0), gender,
                minAge, maxAge, true, joinType, 5, squadId);
    }


    public ResultActions updateSquadNoToken(int id, int minAge, int maxAge, Gender gender, JoinType joinType, Long squadId) throws Exception {
        return updateSquadWithoutToken("자바 스터디", "프로그래밍", "매주 토요일 자바 스터디 합니다.",
                "서울", "강남구",
                LocalDate.now().plusDays(3), LocalTime.of(10, 0, 0), gender,
                minAge, maxAge, true, joinType, 5, squadId);
    }

    public ResultActions updateSquadInvalidTitleCategoryDescription(int id, int minAge, int maxAge, Gender gender, JoinType joinType, Long squadId) throws Exception {
        return updateSquadInvalid(id, null, null, null, "서울", "강남구",
                LocalDate.now().plusDays(3), LocalTime.of(10, 0, 0), gender,
                minAge, maxAge, true, joinType, 5, squadId
                );
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



    private void addPendingSquadMember(String email, Long squadId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        Squad squad = squadRepository.findById(squadId).orElseThrow();

        SquadMember squadMember = new SquadMember();
        squadMember.setUser(user);
        squadMember.setSquad(squad);
        squadMember.setAttendanceStatus(AttendanceStatus.PENDING);
        squad.getMembers().add(squadMember);
        squadMemberRepository.save(squadMember);
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