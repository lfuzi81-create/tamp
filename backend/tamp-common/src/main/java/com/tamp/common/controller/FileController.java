package com.tamp.common.controller;

import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN", "INVESTOR"})
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam("category") String category) {
        String url = fileService.upload(file, category);
        return Result.ok(url);
    }

    @DeleteMapping
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN"})
    public Result<Void> delete(@RequestParam("fileUrl") String fileUrl) {
        fileService.delete(fileUrl);
        return Result.ok();
    }
}
