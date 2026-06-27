package com.tamp.common.service;

import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileService {

    @Value("${tamp.file.upload-path:/tmp/tamp-uploads}")
    private String uploadPath;

    @Value("${tamp.file.access-path:/uploads}")
    private String accessPath;

    public String upload(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = UUID.randomUUID().toString() + ext;

        String relativePath = category + "/" + date + "/" + filename;
        Path fullPath = Paths.get(uploadPath, relativePath);

        try {
            Files.createDirectories(fullPath.getParent());
            file.transferTo(fullPath.toFile());
        } catch (IOException e) {
            throw new BizException(ErrorCode.BIZ_OPERATION_FAILED, "文件上传失败");
        }

        return accessPath + "/" + relativePath;
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(accessPath + "/")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "无效的文件URL");
        }

        String relativePath = fileUrl.substring(accessPath.length() + 1);
        Path fullPath = Paths.get(uploadPath, relativePath);

        try {
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            throw new BizException(ErrorCode.BIZ_OPERATION_FAILED, "文件删除失败");
        }
    }
}
