package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.feed.dto.S3TempFileInfo;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.S3Exception;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileService  {

    private final AmazonS3 amazonS3;


    private static final String TEMP_PREFIX = "feed/temp/";
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;



    public void upload(MultipartFile file, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();

            if (file.getContentType() != null) {
                metadata.setContentType(file.getContentType());
            }

            metadata.setContentLength(file.getSize());

            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            log.error("S3 업로드 실패. bucket={}, fileName={}", bucket, fileName, e);
            throw new S3Exception(ErrorCode.S3_FAILED);
        } catch (Exception e) {
            log.error("S3 업로드 중 예외 발생. bucket={}, fileName={}", bucket, fileName, e);
            throw new S3Exception(ErrorCode.S3_FAILED);
        }
    }


    public String uploadAndReturnUrl(MultipartFile file, String fileName) {
        upload(file, fileName);
        return getFullUrl(fileName);
    }


    public void delete(String fileName) {
        try {
            amazonS3.deleteObject(bucket, fileName);
        } catch (Exception e) {
            log.error("S3 삭제 실패. bucket={}, fileName={}", bucket, fileName, e);
            throw new S3Exception(ErrorCode.S3_FAILED);
        }
    }

    public void deleteByUrl(String url) {
        String key = extractKeyFromUrl(url);
        delete(key);
    }

    public String extractKeyFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            String path = uri.getPath();
            if (path == null) path = "";

            if (host != null && host.startsWith(bucket + ".")) {
                return path.startsWith("/") ? path.substring(1) : path;
            }

            String prefix = "/" + bucket + "/";
            if (path.startsWith(prefix)) {
                return path.substring(prefix.length());
            }

            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            log.error("S3 URL에서 key 추출 실패. url={}", url, e);
            throw new S3Exception(ErrorCode.S3_FAILED);
        }
    }

    public String getFullUrl(String fileName) {
        try {
            return amazonS3.getUrl(bucket, fileName).toString();
        } catch (Exception e) {
            log.error("S3 URL 생성 실패. bucket={}, fileName={}", bucket, fileName, e);
            throw new S3Exception(ErrorCode.S3_FAILED);
        }
    }


    //todo 다시 알아가보기
    public List<S3TempFileInfo> findTempFiles() {
        try {
            List<S3TempFileInfo> result = new ArrayList<>();

            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucket)
                    .withPrefix(TEMP_PREFIX)
                    .withMaxKeys(1000);

            ListObjectsV2Result response;

            do {
                response = amazonS3.listObjectsV2(request);

                for (S3ObjectSummary summary : response.getObjectSummaries()) {
                    String key = summary.getKey();

                    // 혹시 폴더 placeholder가 있으면 제외
                    if (key.endsWith("/")) {
                        continue;
                    }

                    String url = getFullUrl(key);
                    Instant lastModified = summary.getLastModified().toInstant();

                    result.add(new S3TempFileInfo(key, url, lastModified));
                }

                request.setContinuationToken(response.getNextContinuationToken());
            } while (response.isTruncated());

            return result;
        } catch (Exception e) {
            log.error("S3 temp 파일 목록 조회 실패. bucket={}, prefix={}", bucket, TEMP_PREFIX, e);
            throw new S3Exception(ErrorCode.S3_FAILED);
        }
    }




}

