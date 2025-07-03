package FeedStudy.StudyFeed;

import FeedStudy.StudyFeed.auth.service.AuthCodeService;
import FeedStudy.StudyFeed.block.entity.Block;
import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.feed.dto.FeedRequest;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FeedTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected AuthCodeService authCodeService;

    @Autowired
    protected S3FileService s3FileService;

    @Autowired
    protected FeedImageRepository feedImageRepository;

    @Autowired
    protected FeedRepository feedRepository;

    @Autowired
    protected BlockRepository blockRepository;

    @Autowired
    protected FeedCommentRepository feedCommentRepository;


    protected Map<String, String> tokenMap = new HashMap<>();

    private final List<String> uploadedImageNames = new ArrayList<>();

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

    @AfterEach
    void cleanUp() throws Exception {
        for (String name : uploadedImageNames) {
            s3FileService.delete(name);
        }
    }


    public String getToken(int id) {
        return tokenMap.get("test@" + id + "test.com");
    }




    @Test
    void createFeed_success_withImage() throws Exception {
        String token = getToken(1);
        MockMultipartFile imageFile = getTestImageFile();


        mockMvc.perform(
                        multipart("/api/feed/create")
                                .file(imageFile)
                                .param("content", "이건 실제 이미지 업로드 테스트입니다.")
                                .param("category", "프로그래밍")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk());


        saveUploadedImagesNames();

    }


    @Test
    void createFeed_fail_whenMissingContentAndCategory() throws Exception {
        String token = getToken(1);
        MockMultipartFile imageFile = getTestImageFile();

        mockMvc.perform(multipart("/api/feed/create")
                .file(imageFile)
                .header("Authorization", token))
                .andExpect(status().isBadRequest());


        saveUploadedImagesNames();

    }


    @Test
    void getFeedDetails_success() throws Exception {

        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();

        System.out.println("🙋 유저 닉네임: " + user.getNickName());

        Feed feed = createDummyFeed(user, "더미 피드입니다.", "운동");

        mockMvc.perform(get("/api/feed/" + feed.getId())
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("더미 피드입니다."))
                .andExpect(jsonPath("$.category").value("운동"))
                .andExpect(jsonPath("$.like").value(false))
        .andExpect(jsonPath("$.isMine").value(true));
    }


    @Test
    void modifyFeed_success() throws Exception {
        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();
        Feed feed = createDummyFeed(user, "원본 내용", "운동");

        MockMultipartFile newImage = new MockMultipartFile(
                "addedImages",
                "dummy-" + System.currentTimeMillis() + ".jpg",
                "image/jpeg",
                getTestImageFile().getBytes()
        );

        mockMvc.perform(multipart("/api/feed/modify/" + feed.getId())
                        .file(newImage)
                        .param("content", "수정된 내용입니다.")
                        .param("category", "스터디")
                        .with(request -> {
                            request.setMethod("PUT"); // multipart는 기본 POST이므로 PUT으로 변경
                            return request;
                        })
                        .header("Authorization", token)
                )
                .andExpect(status().isOk());
        // 결과 검증: DB에서 피드 다시 조회 후 검증 (선택)
        Feed updated = feedRepository.findById(feed.getId()).orElseThrow();
        assertEquals("수정된 내용입니다.", updated.getContent());
        assertEquals("스터디", updated.getCategory());

        saveUploadedImagesNames(); // 새 이미지 이름 저장
    }

    @Test
    void deleteFeed_success() throws Exception {
        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();

        Feed feed = createDummyFeed(user, "삭제 테스트용 피드", "운동");

        mockMvc.perform(delete("/api/feed/delete/" + feed.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk());

        boolean exists = feedRepository.findById(feed.getId()).isPresent();
        assertFalse(exists); // 존재하지 않아야 함


    }

    @Test
    void deleteFeed_fail_whenUserIsNotOwner() throws Exception {
        User owner = userRepository.findByEmail("test@1test.com").orElseThrow();
        Feed feed = createDummyFeed(owner, "다른 사람 피드", "운동");

        String attackerToken = getToken(2);

        mockMvc.perform(delete("/api/feed/delete/" + feed.getId())
                        .header("Authorization", attackerToken))
                .andExpect(status().isForbidden()); // 또는 status().isBadRequest() 예외 처리 방식에 따라

    }

    @Test
    void likeFeed_success_firstLike() throws Exception {
        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();
        Feed feed = createDummyFeed(user, "좋아요 테스트", "프로그래밍");

        mockMvc.perform(get("/api/feed/like/" + feed.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.like").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void likeFeed_success_toggleOff() throws Exception {
        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();
        Feed feed = createDummyFeed(user, "좋아요 토글 테스트", "운동");

        // 먼저 좋아요 누르기
        mockMvc.perform(get("/api/feed/like/" + feed.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 다시 한 번 눌러서 취소
        mockMvc.perform(get("/api/feed/like/" + feed.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.like").value(false))
                .andExpect(jsonPath("$.count").value(0));
    }


    @Test
    void getMyFeeds_success() throws Exception {
        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();

        // 피드 여러 개 생성
        for (int i = 1; i <= 3; i++) {
            createDummyFeed(user, "내 피드 내용 " + i, "운동");
        }

        mockMvc.perform(get("/api/feed/mine")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(false)) // 3개밖에 없으니 false
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(3))
                .andExpect(jsonPath("$.list[0].content").value("내 피드 내용 3"))
                .andExpect(jsonPath("$.list[0].isMine").value(true));
    }

    @Test
    void getMyFeeds_pagnation_success() throws Exception {
        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();

        // 피드 여러 개 생성
        for (int i = 1; i <= 15; i++) {
            createDummyFeed(user, "내 피드 내용 " + i, "운동");
        }

        mockMvc.perform(get("/api/feed/mine")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.list.length()").value(5));
    }

    @Test
    void getHomeFeeds_success() throws Exception {
        String token = getToken(1); // test@1test.com
        User user2 = userRepository.findByEmail("test@2test.com").orElseThrow();
        User user3 = userRepository.findByEmail("test@3test.com").orElseThrow();

        createDummyFeed(user2, "user2의 피드입니다.", "운동");
        createDummyFeed(user3, "user3의 피드입니다.", "스터디");

        mockMvc.perform(get("/api/feed/home")
                        .param("category", "전체")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].isMine").value(false))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void getHomeFeeds_excludesBlockedUsersFeeds() throws Exception {
        String viewerToken = getToken(1); // test@1test.com
        User viewer = userRepository.findByEmail("test@1test.com").orElseThrow();
        User blocked = userRepository.findByEmail("test@2test.com").orElseThrow(); // 차단 대상

        // 차단된 유저와 일반 유저의 피드 생성
        createDummyFeed(blocked, "차단된 유저 피드", "운동");
        createDummyFeed(viewer, "나의 피드", "운동");

        // viewer가 blocked를 차단했다고 가정 (Block 엔티티 직접 저장)
        blockRepository.save(new Block(viewer, blocked)); // viewer -> blocked

        // 홈 피드 요청
        mockMvc.perform(get("/api/feed/home")
                        .param("category", "전체")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(jsonPath("$.list[0].content").value("나의 피드"));
    }

    @Test
    void getHomeFeeds_filteredByCategory_success() throws Exception {
        String token = getToken(1); // test@1test.com
        User user2 = userRepository.findByEmail("test@2test.com").orElseThrow();
        User user3 = userRepository.findByEmail("test@3test.com").orElseThrow();

        // 각각 다른 카테고리로 피드 작성
        createDummyFeed(user2, "user2 운동 피드", "운동");
        createDummyFeed(user3, "user3 스터디 피드", "스터디");

        // 카테고리 = 운동 으로 필터링
        mockMvc.perform(get("/api/feed/home")
                        .param("category", "운동")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(jsonPath("$.list[0].content").value("user2 운동 피드"))
                .andExpect(jsonPath("$.list[0].category").value("운동"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void getOtherFeeds_success() throws Exception {
        String viewerToken = getToken(1); // test@1test.com
        User target = userRepository.findByEmail("test@2test.com").orElseThrow(); // 피드를 조회당할 유저

        // target 유저가 피드 작성
        for (int i = 1; i <= 3; i++) {
            createDummyFeed(target, "타겟 유저 피드 " + i, "운동");
        }

        // 피드 조회
        mockMvc.perform(post("/api/feed/others/" + target.getId()) // ✅ POST + /others/{id}
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(3))
                .andExpect(jsonPath("$.list[0].content").value("타겟 유저 피드 3"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }


    @Test
    void createComment_success() throws Exception {
        String token = getToken(1);
        User user = userRepository.findByEmail("test@1test.com").orElseThrow();
        Feed feed = createDummyFeed(user, "테스트 피드", "운동");

        mockMvc.perform(multipart("/api/feed/createcomment")
                        .param("content", "댓글 내용입니다.")
                        .param("feedPid", String.valueOf(feed.getId()))
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void getReplies_success_includesDeletedReplies() throws Exception {
        String token = getToken(1);
        User parentUser = userRepository.findByEmail("test@1test.com").orElseThrow();
        Feed feed = createDummyFeed(parentUser, "댓글 테스트 피드", "운동");

        // 1. 부모 댓글 생성
        FeedComment parentComment = new FeedComment(parentUser, feed, "부모 댓글", null);
        feedCommentRepository.save(parentComment);

        // 2. 대댓글 2개 생성 (1개는 소프트 삭제 예정)
        FeedComment reply1 = new FeedComment(parentUser, feed, "첫 번째 대댓글", parentComment);
        FeedComment reply2 = new FeedComment(parentUser, feed, "두 번째 대댓글", parentComment);
        feedCommentRepository.save(reply1);
        feedCommentRepository.save(reply2);

        // 3. 두 번째 대댓글을 soft delete 처리
        reply2.markAsDeleted();

        // 4. /api/feed/replies/{parentId} 로 요청
        mockMvc.perform(get("/api/feed/comment/" + parentComment.getId() + "/replies")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].content").value("첫 번째 대댓글"))
                .andExpect(jsonPath("$.list[1].content").value("작성자가 댓글을 삭제했습니다."));
    }


    private void saveUploadedImagesNames() {
        List<FeedImage> uploadedImages = feedImageRepository.findAll();

        for (FeedImage image : uploadedImages) {
            System.out.println("✅ Uploaded image: " + image.getUniqueName());
            uploadedImageNames.add(image.getUniqueName()); // 나중에 @AfterEach에서 삭제용으로 사용 가능
        }
    }


    private MockMultipartFile getTestImageFile() throws IOException {
        Path imagePath = Paths.get("src/main/resources/test-image.jpg");
        byte[] bytes = Files.readAllBytes(imagePath);

        return new MockMultipartFile(
                "addedImages",
                "test-image.jpg",
                "image/jpeg",
                bytes
        );
    }

    private Feed createDummyFeed(User user, String content, String category) throws Exception {
        FeedRequest request = new FeedRequest();
        request.setContent(content);
        request.setCategory(category);

        MockMultipartFile mockImage = getTestImageFile();

        // 2. S3 업로드
        String uniqueName = "dummy-" + System.currentTimeMillis() + ".jpg"; // 혹은 UUID 등
        s3FileService.upload(mockImage, uniqueName);
        uploadedImageNames.add(uniqueName);


        String imageUrl = s3FileService.getFullUrl(uniqueName);
        FeedImage feedImage = new FeedImage(imageUrl, mockImage.getOriginalFilename());
        List<FeedImage> imageList = List.of(feedImage);

        Feed feed = new Feed(user, request, imageList);
        return feedRepository.save(feed);


    }


}


