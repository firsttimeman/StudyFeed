package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class LocalFileService implements FileService {

    @Value("/var/www/html/upload")
    private String uploadDir;

    @Override
    public void upload(MultipartFile file, String fileName) {
        try {
            Path path = Paths.get(uploadDir, fileName);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
        } catch (IOException e) {
            log.error("파일 저장 실패" + e);
            throw new FeedException(ErrorCode.FILE_CANNOT_BE_UPLOADED);
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            Path path = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("파일 삭제 실패: " + e);
        }
    }
}
