package com.studyai.controller;

import com.studyai.dto.Result;
import com.studyai.entity.Student;
import com.studyai.repository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private StudentRepository studentRepository;

    private static final List<String> IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    private static final long MAX_SIZE = 10 * 1024 * 1024;

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        if (!IMAGE_TYPES.contains(file.getContentType())) {
            return Result.error("仅支持 JPG、PNG、GIF、WebP、BMP 格式的图片");
        }

        if (file.getSize() > MAX_SIZE) {
            return Result.error("图片大小不能超过 10MB");
        }

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            String projectRoot = System.getProperty("user.dir");
            String uploadDirPath = projectRoot + File.separator + "uploads" + File.separator + "avatars";
            File dir = new File(uploadDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                ext = ".png";
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, filename);

            file.transferTo(dest);

            String avatarUrl = "/uploads/avatars/" + filename;
            Student student = studentRepository.findById(userId).orElse(null);
            if (student != null) {
                student.setAvatar(avatarUrl);
                studentRepository.save(student);
            }

            return Result.success(avatarUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 通用文件上传接口
     * 支持图片、文档、PDF、PPT等多种格式
     */
    @PostMapping("/file")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        if (file.getSize() > MAX_SIZE) {
            return Result.error("文件大小不能超过 10MB");
        }

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            String projectRoot = System.getProperty("user.dir");
            String uploadDirPath = projectRoot + File.separator + "uploads" + File.separator + "files";
            File dir = new File(uploadDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                ext = ".bin";
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, filename);

            file.transferTo(dest);

            String fileUrl = "/uploads/files/" + filename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
