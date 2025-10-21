package com.windcore.service;

import com.windcore.dto.FileUploadResult;
import com.windcore.dto.VirusScanResult;
import com.windcore.exception.FileUploadException;
import com.windcore.model.FileMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class SecureFileUploadService {

    @Value("${file.upload.dir:/data/uploads}")
    private String uploadDir;

    @Autowired
    private VirusScanner virusScanner;

    /**
     * 允许的文件类型白名单
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt"
    );

    /**
     * 文件头签名验证
     */
    private static final Map<String, String> FILE_SIGNATURES = Map.of(
            "jpg", "FFD8FF",
            "png", "89504E47",
            "pdf", "25504446",
            "doc", "D0CF11E0",
            "docx", "504B0304"
    );

    /**
     * 上传文件主方法
     * @param file 上传的文件
     * @param businessType 业务类型
     * @return 文件上传结果
     */
    public FileUploadResult uploadFile(MultipartFile file, String businessType) {
        // 1. 基础验证
        validateFileBasic(file);

        // 2. 安全扫描
        securityScan(file);

        // 3. 文件类型验证
        validateFileType(file);

        // 4. 病毒扫描
        virusScan(file);

        // 5. 保存文件
        return saveFileSecurely(file, businessType);
    }

    /**
     * 文件基本验证
     * @param file 文件
     */
    private void validateFileBasic(MultipartFile file) {
        // 1.文件是否为空
        if (file.isEmpty()) {
            throw new FileUploadException("文件不能为空");
        }
        // 2.是否超过文件大小
        if (file.getSize() > 50 * 1024 * 1024) { // 50MB
            throw new FileUploadException("文件大小超过限制");
        }
        // 3. 文件名是否合法
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new FileUploadException("文件名不合法");
        }
    }

    /**
     * 安全扫描
     * @param file 文件
     */
    private void securityScan(MultipartFile file) {
        String filename = file.getOriginalFilename();

        // 检查文件扩展名
        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileUploadException("不支持的文件类型: " + extension);
        }

        // 检查文件内容类型
        String contentType = file.getContentType();
        if (!isContentTypeAllowed(contentType, extension)) {
            throw new FileUploadException("文件内容类型不匹配");
        }
    }

    /**
     * 文件类型验证
     * @param file 文件
     */
    private void validateFileType(MultipartFile file) {
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            String expectedSignature = FILE_SIGNATURES.get(extension.toLowerCase());

            if (expectedSignature != null) {
                byte[] fileHeader = new byte[4];
                try (InputStream inputStream = file.getInputStream()) {
                    inputStream.read(fileHeader);
                }

                String actualSignature = bytesToHex(fileHeader);
                if (!actualSignature.startsWith(expectedSignature)) {
                    throw new FileUploadException("文件类型与内容不匹配");
                }
            }
        } catch (IOException e) {
            throw new FileUploadException("文件类型验证失败", e);
        }
    }

    /**
     * 病毒扫描
     * @param file 文件
     */
    private void virusScan(MultipartFile file) {
        // 集成病毒扫描服务（ClamAV等）
        try {
            VirusScanResult result = virusScanner.scan(file.getBytes());
            if (!result.isClean()) {
                log.warn("检测到恶意文件: {}, 威胁: {}",
                        file.getOriginalFilename(), result.getThreats());
                throw new FileUploadException("文件包含恶意内容");
            }
        } catch (IOException e) {
            throw new FileUploadException("病毒扫描失败", e);
        }
    }

    /**
     * 安全保存文件
     * @param file 文件
     * @param businessType 业务类型
     * @return 文件上传结果
     */
    private FileUploadResult saveFileSecurely(MultipartFile file, String businessType) {
        try {
            // 生成安全的文件名
            String safeFilename = generateSafeFilename(file.getOriginalFilename());
            String filePath = Paths.get(uploadDir, businessType, safeFilename).toString();

            // 创建目录
            File destFile = new File(filePath);
            destFile.getParentFile().mkdirs();

            // 保存文件
            file.transferTo(destFile);

            // 设置文件权限
            setFilePermissions(destFile);

            // 记录文件元数据
            FileMetadata metadata = saveFileMetadata(file, safeFilename, businessType);

            return FileUploadResult.success(metadata);

        } catch (IOException e) {
            throw new FileUploadException("文件保存失败", e);
        }
    }

    /**
     * 生成安全的文件名
     * @param originalFilename 原始文件名
     * @return 安全的文件名
     */
    private String generateSafeFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8);

        return timestamp + "_" + random + "." + extension;
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 检查内容类型是否允许
     * @param contentType 内容类型
     * @param extension 扩展名
     * @return 是否允许
     */
    private boolean isContentTypeAllowed(String contentType, String extension) {
        if (contentType == null) {
            return false;
        }
        
        // 基础验证，可以根据需要扩展更详细的映射
        return contentType.startsWith("image/") || 
               contentType.startsWith("application/") || 
               contentType.startsWith("text/");
    }

    /**
     * 字节转十六进制
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 设置文件权限
     * @param file 文件
     */
    private void setFilePermissions(File file) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows 文件权限设置
                file.setReadable(true, true);
                file.setWritable(true, true);
                file.setExecutable(false, false);
            } else {
                // Unix/Linux 文件权限设置
                Files.setPosixFilePermissions(file.toPath(), 
                    PosixFilePermissions.fromString("rw-------"));
            }
        } catch (IOException e) {
            log.error("设置文件权限失败", e);
        }
    }

    /**
     * 保存文件元数据
     * @param file 文件
     * @param safeFilename 安全文件名
     * @param businessType 业务类型
     * @return 文件元数据
     */
    private FileMetadata saveFileMetadata(MultipartFile file, String safeFilename, String businessType) {
        FileMetadata metadata = new FileMetadata();
        metadata.setId(UUID.randomUUID().toString());
        metadata.setFileName(file.getOriginalFilename());
        metadata.setSafeFileName(safeFilename);
        metadata.setFilePath(Paths.get(uploadDir, businessType, safeFilename).toString());
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setBusinessType(businessType);
        metadata.setUploadTime(LocalDateTime.now());

        // 这里通常会保存到数据库
        log.info("保存文件元数据: {}", metadata);

        return metadata;
    }
}