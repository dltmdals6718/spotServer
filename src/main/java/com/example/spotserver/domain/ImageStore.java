package com.example.spotserver.domain;

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

    @Value("${posterImg.dir}")
    private String posterImgDir;

    @Value("${locationImg.dir}")
    private String locationImgDir;

    @Value("${memberImg.dir}")
    private String memberImgDir;


    public List<PosterImage> storePosterImages(List<MultipartFile> images) throws IOException {
        List<PosterImage> result = new ArrayList<>();

        for (MultipartFile image : images) {
            String uploadFileName = image.getOriginalFilename();

            String uuid = UUID.randomUUID().toString();
            int pos = uploadFileName.indexOf(".");
            String ext = uploadFileName.substring(pos + 1);

            String storeFileName = uuid + "." + ext;

            image.transferTo(new File(getPosterImgFullPath(storeFileName)));
            result.add(new PosterImage(uploadFileName, storeFileName));
        }
        return result;
    }

    public List<LocationImage> storeLocationImages(List<MultipartFile> images) throws IOException {
        List<LocationImage> result = new ArrayList<>();

        for (MultipartFile image : images) {
            String uploadFileName = image.getOriginalFilename();

            String uuid = UUID.randomUUID().toString();
            int pos = uploadFileName.indexOf(".");
            String ext = uploadFileName.substring(pos + 1);

            String storeFileName = uuid + "." + ext;

            image.transferTo(new File(getLocationImgFullPath(storeFileName)));
            result.add(new LocationImage(uploadFileName, storeFileName));
        }
        return result;
    }

    public MemberImage storeMemberImage(MultipartFile image) throws IOException {

        String uploadFileName = image.getOriginalFilename();;
        String uuid = UUID.randomUUID().toString();

        String ext = getFileExtension(uploadFileName);

        String storeFileName = uuid + "." + ext;

        image.transferTo(new File(getMemberImgFullPath(storeFileName)));

        MemberImage memberImage = new MemberImage(uploadFileName, storeFileName);
        return memberImage;
    }

    public String getLocationImgFullPath(String imageFileName) {
        return locationImgDir+imageFileName;
    }

    public String getPosterImgFullPath(String imageFileName) {
        return posterImgDir+imageFileName;
    }

    public String getMemberImgFullPath(String imageFileName) {
        return memberImgDir+imageFileName;
    }

    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.')+1);
    }
}
