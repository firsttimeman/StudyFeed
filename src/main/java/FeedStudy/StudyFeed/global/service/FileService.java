package FeedStudy.StudyFeed.global.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    void upload(MultipartFile file, String fileName);

    void delete(String fileName);

    default String getFullUrl(String fileName) {
        throw new UnsupportedOperationException("getFullUrl은 S3Service에서만 제공됩니다");
    }
}
