package FeedStudy.StudyFeed.feed.entity;

import FeedStudy.StudyFeed.feed.dto.FeedEditRequest;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Feed extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String category;

    @Lob
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int reportCount = 0;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedReport> reports = new ArrayList<>();

    public Feed(User user, FeedEditRequest request , List<FeedImage> images) {
        this.user = user;
        this.category = request.getCategory();
        addImage(images);
    }

    public ImageUpdatedResult update(FeedEditRequest request) {
        this.content = request.getContent();
        this.category = request.getCategory();
        ImageUpdatedResult result = findImageUpdatedResult(request.getAddImages(), request.getDeletedImages());
        addImage(result.getAddedImages());
        deleteImages(result.getDeletedImages());
        return result;
    }


    private void addImage(List<FeedImage> images) {
        images.stream().forEach(x -> {
            this.images.add(x);
            x.initFeed(this);
        });
    }

    private void deleteImages(List<FeedImage> images) {
        images.stream().forEach(dl -> this.images.remove(dl));
    }


    private ImageUpdatedResult  findImageUpdatedResult(List<MultipartFile> addImages, List<Integer> deletedImages) {
        List<FeedImage> addImage = convertImageFilesToImages(addImages);
        List<FeedImage> deleteImage = convertImageIdsToImages(deletedImages);
        return new ImageUpdatedResult(addImages, addImage, deleteImage);

    }

    private List<FeedImage> convertImageIdsToImages(List<Integer> imagesId) {
        return imagesId.stream().map(id -> convertImageIdToImage(id)).filter(x -> x.isPresent()).map(x -> x.get()).toList();
    }

    private Optional<FeedImage> convertImageIdToImage(int id) {
        return this.images.stream().filter(x -> x.getId() == id).findAny();
    }

    private List<FeedImage> convertImageFilesToImages(List<MultipartFile> images) {
       return images.stream().map(x -> new FeedImage(x.getOriginalFilename())).toList();
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if(this.likeCount > 0){
            this.likeCount--;
        }
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    public void decreaseReportCount() {
        if(this.reportCount > 0){
            this.reportCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if(this.commentCount > 0){
            this.commentCount--;
        }
    }




    @Getter
    @AllArgsConstructor
    public static class ImageUpdatedResult{
        private List<MultipartFile> addedImageFiles;
        private List<FeedImage> addedImages;
        private List<FeedImage> deletedImages;
    }


}
