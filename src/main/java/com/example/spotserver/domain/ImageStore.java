package com.example.spotserver.domain;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ImageStore {

    private String posterImgDir = "posterImg/";

    private String locationImgDir = "locationImg/";

    private String memberImgDir = "memberImg/";
    private AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Autowired
    public ImageStore(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public List<PosterImage> storePosterImages(List<MultipartFile> images) throws IOException {
        List<PosterImage> result = new ArrayList<>();

        for (MultipartFile image : images) {
            String uploadFileName = image.getOriginalFilename();

            String uuid = UUID.randomUUID().toString();
            String ext = getFileExtension(uploadFileName);
            String storeFileName = uuid + "." + ext;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(image.getContentType());
            metadata.setContentLength(image.getSize());
            amazonS3Client.putObject(bucket, posterImgDir + storeFileName, image.getInputStream(), metadata);

            result.add(new PosterImage(uploadFileName, storeFileName));
        }
        return result;
    }

    public List<LocationImage> storeLocationImages(List<MultipartFile> images) throws IOException {
        List<LocationImage> result = new ArrayList<>();

        for (MultipartFile image : images) {
            String uploadFileName = image.getOriginalFilename();

            String uuid = UUID.randomUUID().toString();
            String ext = getFileExtension(uploadFileName);
            String storeFileName = uuid + "." + ext;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(image.getContentType());
            metadata.setContentLength(image.getSize());
            amazonS3Client.putObject(bucket, locationImgDir + storeFileName, image.getInputStream(), metadata);
            result.add(new LocationImage(uploadFileName, storeFileName));
        }
        return result;
    }

    public MemberImage storeMemberImage(MultipartFile image) throws IOException {

        String uploadFileName = image.getOriginalFilename();;

        String uuid = UUID.randomUUID().toString();
        String ext = getFileExtension(uploadFileName);
        String storeFileName = uuid + "." + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        amazonS3Client.putObject(bucket, memberImgDir + storeFileName, image.getInputStream(), metadata);

        MemberImage memberImage = new MemberImage(uploadFileName, storeFileName);
        return memberImage;
    }

    public void deletePosterImage(PosterImage posterImage) {
        String storeFileName = posterImage.getStoreFileName();
        amazonS3Client.deleteObject(bucket, posterImgDir + storeFileName);
    }

    public void deleteLocationImage(LocationImage locationImage) {
        String storeFileName = locationImage.getStoreFileName();
        amazonS3Client.deleteObject(bucket, locationImgDir + storeFileName);
    }

    public void deleteMemberImage(MemberImage memberImage) {
        String storeFileName = memberImage.getStoreFileName();
        amazonS3Client.deleteObject(bucket, memberImgDir + storeFileName);
    }

    public String getMemberImgFullPath(String imageStoreFileName) {
        return amazonS3Client.getResourceUrl(bucket, memberImgDir + imageStoreFileName);
    }

    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.')+1);
    }

    public String getPosterImgDir() {
        return posterImgDir;
    }

    public String getLocationImgDir() {
        return locationImgDir;
    }

    public String getMemberImgDir() {
        return memberImgDir;
    }

    public String getBucket() {
        return bucket;
    }
}
