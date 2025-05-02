package FeedStudy.StudyFeed.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("업로드 실패", e);
        }
    }

    @Override
    public void delete(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
    }

    @Override
    public String getFullUrl(String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }
}

