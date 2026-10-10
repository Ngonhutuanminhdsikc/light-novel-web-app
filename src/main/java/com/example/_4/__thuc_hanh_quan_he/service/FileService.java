package com.example._4.__thuc_hanh_quan_he.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

    // Đường dẫn trỏ thẳng vào thư mục static để ảnh hiển thị ngay lập tức
    private final String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // Tạo thư mục nếu chưa có
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        // Tạo tên file duy nhất bằng UUID để tránh trùng lặp
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        // Copy file vật lý vào thư mục
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}