package com.windcore.service.impl;

import com.windcore.model.FileMetadata;
import com.windcore.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 文件服务实现类
 * 注意：这是一个简单的内存实现，实际项目中应该使用数据库
 * 
 * @author windcore
 */
@Service
public class FileServiceImpl implements FileService {
    
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    
    // 内存存储，实际项目中应该使用数据库
    private final Map<String, FileMetadata> fileStore = new ConcurrentHashMap<>();
    private final Map<String, String> md5Index = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public FileMetadata findByMd5(String md5) {
        if (md5 == null || md5.trim().isEmpty()) {
            return null;
        }
        
        String id = md5Index.get(md5);
        if (id != null) {
            return fileStore.get(id);
        }
        return null;
    }
    
    @Override
    public FileMetadata findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return fileStore.get(id);
    }
    
    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        if (fileMetadata == null) {
            throw new IllegalArgumentException("文件元数据不能为空");
        }
        
        // 生成ID
        if (fileMetadata.getId() == null || fileMetadata.getId().trim().isEmpty()) {
            fileMetadata.setId(String.valueOf(idGenerator.getAndIncrement()));
        }
        
        // 设置上传时间
        if (fileMetadata.getUploadTime() == null) {
            fileMetadata.setUploadTime(LocalDateTime.now());
        }
        
        // 保存到存储
        fileStore.put(fileMetadata.getId(), fileMetadata);
        
        // 更新MD5索引
        if (fileMetadata.getFileMd5() != null) {
            md5Index.put(fileMetadata.getFileMd5(), fileMetadata.getId());
        }
        
        log.info("保存文件元数据: {}", fileMetadata);
        return fileMetadata;
    }
    
    @Override
    public FileMetadata update(FileMetadata fileMetadata) {
        if (fileMetadata == null || fileMetadata.getId() == null) {
            throw new IllegalArgumentException("文件元数据或ID不能为空");
        }
        
        FileMetadata existing = fileStore.get(fileMetadata.getId());
        if (existing == null) {
            throw new IllegalArgumentException("文件不存在: " + fileMetadata.getId());
        }
        
        // 更新字段
        if (fileMetadata.getFileName() != null) {
            existing.setFileName(fileMetadata.getFileName());
        }
        if (fileMetadata.getSafeFileName() != null) {
            existing.setSafeFileName(fileMetadata.getSafeFileName());
        }
        if (fileMetadata.getFilePath() != null) {
            existing.setFilePath(fileMetadata.getFilePath());
        }
        if (fileMetadata.getFileType() != null) {
            existing.setFileType(fileMetadata.getFileType());
        }
        if (fileMetadata.getFileSize() > 0) {
            existing.setFileSize(fileMetadata.getFileSize());
        }
        if (fileMetadata.getBusinessType() != null) {
            existing.setBusinessType(fileMetadata.getBusinessType());
        }
        
        log.info("更新文件元数据: {}", existing);
        return existing;
    }
    
    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        FileMetadata fileMetadata = fileStore.remove(id);
        if (fileMetadata != null) {
            // 移除MD5索引
            if (fileMetadata.getFileMd5() != null) {
                md5Index.remove(fileMetadata.getFileMd5());
            }
            log.info("删除文件元数据: {}", fileMetadata);
            return true;
        }
        return false;
    }
    
    @Override
    public List<FileMetadata> findByBusinessType(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return fileStore.values().stream()
                .filter(file -> businessType.equals(file.getBusinessType()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FileMetadata> findByFileNameLike(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String keyword = fileName.toLowerCase();
        return fileStore.values().stream()
                .filter(file -> file.getFileName() != null && 
                               file.getFileName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByMd5(String md5) {
        return md5 != null && md5Index.containsKey(md5);
    }
    
    @Override
    public long count() {
        return fileStore.size();
    }
    
    @Override
    public long countByBusinessType(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return 0;
        }
        
        return fileStore.values().stream()
                .filter(file -> businessType.equals(file.getBusinessType()))
                .count();
    }
}