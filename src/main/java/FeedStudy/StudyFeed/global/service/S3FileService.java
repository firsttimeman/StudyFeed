package FeedStudy.StudyFeed.global.service;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service("s3FileService")
@Primary
@RequiredArgsConstructor
public class S3FileService implements FileService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public void upload(MultipartFile file, String fileName) {
        try{
            ObjectMetadata metadata = new ObjectMetadata();

            if(file.getContentType() != null) {
                metadata.setContentType(file.getContentType());
            }

            metadata.setContentLength(file.getSize());

            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("업로드 실패", e);
        }
    }

    public String uploadAndReturnUrl(MultipartFile file, String fileName) {
        upload(file, fileName);
        return getFullUrl(fileName);
    }

    @Override
    public void delete(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
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
            if(path == null) path = "";

            if(host != null && host.startsWith(bucket + ".")) {
                return path.startsWith("/") ? path.substring(1) : path;
            }

            String prefix = "/" + bucket + "/";
            if (path.startsWith(prefix)) {
                return path.substring(prefix.length());
            }

            // 기타 프리사인 URL 변형 등 → 최후의 보정: 맨 앞 슬래시 제거
            return path.startsWith("/") ? path.substring(1) : path;
        }catch (Exception e) {
            throw new IllegalArgumentException("S3 URL에서 key 추출 실패: " + url, e);
        }
    }

    @Override
    public String getFullUrl(String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }




}

