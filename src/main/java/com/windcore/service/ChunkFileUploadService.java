package com.windcore.service;

import com.windcore.dto.ChunkUploadResult;
import com.windcore.dto.FileChunk;
import com.windcore.dto.UploadProgress;
import com.windcore.exception.FileUploadException;
import com.windcore.model.FileMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 分片文件上传服务
 * 
 * @author windcore
 */
@Service
@Slf4j
public class ChunkFileUploadService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private final FileService fileService;
    
    private static final String UPLOAD_SESSION_KEY = "upload:session:";
    private static final String UPLOAD_PROGRESS_KEY = "upload:progress:";
    private static final String UPLOAD_TOTAL_CHUNKS_KEY = "upload:total:";
    private static final String CHUNK_TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "chunks";
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    public ChunkFileUploadService(RedisTemplate<String, Object> redisTemplate, FileService fileService) {
        this.redisTemplate = redisTemplate;
        this.fileService = fileService;
    }

    /**
     * 上传分片
     */
    public ChunkUploadResult uploadChunk(FileChunk chunk) {
        // 验证分片
        validateChunk(chunk);
        
        // 保存分片
        saveChunk(chunk);
        
        // 更新上传进度
        updateUploadProgress(chunk);
        
        // 检查是否所有分片都上传完成
        if (isUploadComplete(chunk.getFileMd5())) {
            return mergeChunks(chunk.getFileMd5());
        }
        
        return ChunkUploadResult.inProgress(chunk.getChunkNumber());
    }
    
    /**
     * 验证分片
     */
    private void validateChunk(FileChunk chunk) {
        if (chunk == null) {
            throw new FileUploadException("分片数据不能为空");
        }
        
        if (chunk.getFile() == null || chunk.getFile().isEmpty()) {
            throw new FileUploadException("分片文件不能为空");
        }
        
        // 验证分片大小
        if (chunk.getData().length > 5 * 1024 * 1024) { // 5MB
            throw new FileUploadException("分片大小超过限制");
        }
        
        // 验证分片序号
        if (chunk.getChunkNumber() < 0 || chunk.getChunkNumber() >= chunk.getTotalChunks()) {
            throw new FileUploadException("分片序号不合法");
        }
        
        // 验证MD5
        String calculatedMd5 = calculateMd5(chunk.getData());
        if (!calculatedMd5.equals(chunk.getChunkMd5())) {
            throw new FileUploadException("分片MD5校验失败");
        }
        
        log.info("分片验证通过: 文件MD5={}, 分片号={}/{}", 
                chunk.getFileMd5(), chunk.getChunkNumber(), chunk.getTotalChunks());
    }
    
    /**
     * 保存分片
     */
    private void saveChunk(FileChunk chunk) {
        try {
            // 保存分片到临时目录
            Path chunkPath = Paths.get(CHUNK_TEMP_DIR, chunk.getFileMd5(), 
                                     String.valueOf(chunk.getChunkNumber()));
            Files.createDirectories(chunkPath.getParent());
            Files.write(chunkPath, chunk.getData());
            
            // 在Redis中记录分片上传状态
            String sessionKey = getSessionKey(chunk.getFileMd5());
            redisTemplate.opsForSet().add(sessionKey, chunk.getChunkNumber());
            
            // 设置过期时间（24小时）
            redisTemplate.expire(sessionKey, 24, TimeUnit.HOURS);
            
            // 保存总分片数
            String totalChunksKey = getTotalChunksKey(chunk.getFileMd5());
            redisTemplate.opsForValue().set(totalChunksKey, chunk.getTotalChunks(), 24, TimeUnit.HOURS);
            
            log.info("分片保存成功: 文件MD5={}, 分片号={}, 路径={}", 
                    chunk.getFileMd5(), chunk.getChunkNumber(), chunkPath);
            
        } catch (IOException e) {
            throw new FileUploadException("分片保存失败", e);
        }
    }
    
    /**
     * 更新上传进度
     */
    private void updateUploadProgress(FileChunk chunk) {
        String progressKey = getProgressKey(chunk.getFileMd5());
        String sessionKey = getSessionKey(chunk.getFileMd5());
        
        Set<Object> uploadedChunks = redisTemplate.opsForSet().members(sessionKey);
        int uploadedCount = uploadedChunks != null ? uploadedChunks.size() : 0;
        double progress = (double) uploadedCount / chunk.getTotalChunks() * 100;
        
        UploadProgress uploadProgress = UploadProgress.builder()
                .uploadedChunks(uploadedCount)
                .totalChunks(chunk.getTotalChunks())
                .progress(progress)
                .fileMd5(chunk.getFileMd5())
                .status("uploading")
                .build();
        
        redisTemplate.opsForValue().set(progressKey, uploadProgress, 24, TimeUnit.HOURS);
        
        log.info("上传进度更新: 文件MD5={}, 进度={}/{} ({}%)", 
                chunk.getFileMd5(), uploadedCount, chunk.getTotalChunks(), String.format("%.2f", progress));
    }
    
    /**
     * 检查是否上传完成
     */
    private boolean isUploadComplete(String fileMd5) {
        String sessionKey = getSessionKey(fileMd5);
        Set<Object> uploadedChunks = redisTemplate.opsForSet().members(sessionKey);
        Integer totalChunks = getTotalChunksFromRedis(fileMd5);
        
        boolean isComplete = uploadedChunks != null && totalChunks != null && 
                           uploadedChunks.size() == totalChunks;
        
        log.info("检查上传完成状态: 文件MD5={}, 已上传={}, 总数={}, 完成={}", 
                fileMd5, uploadedChunks != null ? uploadedChunks.size() : 0, totalChunks, isComplete);
        
        return isComplete;
    }
    
    /**
     * 合并分片
     */
    private ChunkUploadResult mergeChunks(String fileMd5) {
        try {
            String sessionKey = getSessionKey(fileMd5);
            Set<Object> uploadedChunks = redisTemplate.opsForSet().members(sessionKey);
            Integer totalChunks = getTotalChunksFromRedis(fileMd5);
            
            if (uploadedChunks == null || totalChunks == null || uploadedChunks.size() != totalChunks) {
                throw new FileUploadException("分片不完整，无法合并");
            }
            
            // 合并文件
            File mergedFile = mergeAllChunks(fileMd5, totalChunks);
            
            // 验证合并后的文件MD5
            String mergedFileMd5 = calculateFileMd5(mergedFile);
            if (!fileMd5.equals(mergedFileMd5)) {
                throw new FileUploadException("合并后文件MD5校验失败");
            }
            
            // 清理临时文件
            cleanupTempFiles(fileMd5);
            
            // 保存文件元数据
            FileMetadata metadata = saveMergedFile(mergedFile, fileMd5);
            
            // 清理Redis缓存
            cleanupRedisCache(fileMd5);
            
            log.info("文件合并完成: 文件MD5={}, 文件路径={}", fileMd5, metadata.getFilePath());
            
            return ChunkUploadResult.completed(metadata);
            
        } catch (Exception e) {
            log.error("文件合并失败: 文件MD5=" + fileMd5, e);
            throw new FileUploadException("文件合并失败", e);
        }
    }
    
    /**
     * 合并所有分片
     */
    private File mergeAllChunks(String fileMd5, int totalChunks) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadDir);
        
        // 生成合并后的文件路径
        String fileName = fileMd5 + "_" + System.currentTimeMillis();
        File mergedFile = new File(uploadDir.toFile(), fileName);
        
        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            for (int i = 0; i < totalChunks; i++) {
                Path chunkPath = Paths.get(CHUNK_TEMP_DIR, fileMd5, String.valueOf(i));
                if (!Files.exists(chunkPath)) {
                    throw new IOException("分片文件不存在: " + chunkPath);
                }
                
                byte[] chunkData = Files.readAllBytes(chunkPath);
                fos.write(chunkData);
            }
        }
        
        log.info("分片合并完成: 文件MD5={}, 合并文件={}, 大小={}", 
                fileMd5, mergedFile.getAbsolutePath(), mergedFile.length());
        
        return mergedFile;
    }
    
    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(String fileMd5) {
        try {
            Path tempDir = Paths.get(CHUNK_TEMP_DIR, fileMd5);
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                     .sorted((a, b) -> b.compareTo(a)) // 先删除文件，再删除目录
                     .forEach(path -> {
                         try {
                             Files.deleteIfExists(path);
                         } catch (IOException e) {
                             log.warn("删除临时文件失败: " + path, e);
                         }
                     });
            }
            log.info("临时文件清理完成: 文件MD5={}", fileMd5);
        } catch (IOException e) {
            log.warn("清理临时文件失败: 文件MD5=" + fileMd5, e);
        }
    }
    
    /**
     * 保存合并后的文件元数据
     */
    private FileMetadata saveMergedFile(File mergedFile, String fileMd5) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileMd5(fileMd5);
        metadata.setFileName(mergedFile.getName());
        metadata.setSafeFileName(mergedFile.getName());
        metadata.setFilePath(mergedFile.getAbsolutePath());
        metadata.setFileSize(mergedFile.length());
        metadata.setUploadTime(LocalDateTime.now());
        
        return fileService.save(metadata);
    }
    
    /**
     * 清理Redis缓存
     */
    private void cleanupRedisCache(String fileMd5) {
        String sessionKey = getSessionKey(fileMd5);
        String progressKey = getProgressKey(fileMd5);
        String totalChunksKey = getTotalChunksKey(fileMd5);
        
        redisTemplate.delete(sessionKey);
        redisTemplate.delete(progressKey);
        redisTemplate.delete(totalChunksKey);
        
        log.info("Redis缓存清理完成: 文件MD5={}", fileMd5);
    }
    
    /**
     * 获取上传进度
     */
    public UploadProgress getUploadProgress(String fileMd5) {
        String progressKey = getProgressKey(fileMd5);
        UploadProgress progress = (UploadProgress) redisTemplate.opsForValue().get(progressKey);
        
        if (progress == null) {
            // 如果缓存中没有进度信息，尝试从Redis Set中计算
            String sessionKey = getSessionKey(fileMd5);
            Set<Object> uploadedChunks = redisTemplate.opsForSet().members(sessionKey);
            Integer totalChunks = getTotalChunksFromRedis(fileMd5);
            
            if (uploadedChunks != null && totalChunks != null) {
                double progressValue = (double) uploadedChunks.size() / totalChunks * 100;
                progress = UploadProgress.builder()
                        .uploadedChunks(uploadedChunks.size())
                        .totalChunks(totalChunks)
                        .progress(progressValue)
                        .fileMd5(fileMd5)
                        .status("uploading")
                        .build();
            } else {
                // 没有找到任何上传信息
                progress = UploadProgress.builder()
                        .uploadedChunks(0)
                        .totalChunks(0)
                        .progress(0.0)
                        .fileMd5(fileMd5)
                        .status("not_started")
                        .build();
            }
        }
        
        return progress;
    }
    
    /**
     * 获取已上传的分片列表
     */
    public List<Integer> getUploadedChunks(String fileMd5) {
        String sessionKey = getSessionKey(fileMd5);
        Set<Object> uploadedChunks = redisTemplate.opsForSet().members(sessionKey);
        
        List<Integer> chunkList = new ArrayList<>();
        if (uploadedChunks != null) {
            for (Object chunk : uploadedChunks) {
                if (chunk instanceof Integer) {
                    chunkList.add((Integer) chunk);
                }
            }
        }
        
        return chunkList;
    }
    
    /**
     * 计算MD5
     */
    private String calculateMd5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }
    
    /**
     * 计算文件MD5
     */
    private String calculateFileMd5(File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            return calculateMd5(fileData);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败", e);
        }
    }
    
    /**
     * 获取分片键
     */
    private String getChunkKey(String fileMd5, int chunkNumber) {
        return "chunk:" + fileMd5 + ":" + chunkNumber;
    }
    
    /**
     * 获取会话键
     */
    private String getSessionKey(String fileMd5) {
        return UPLOAD_SESSION_KEY + fileMd5;
    }
    
    /**
     * 获取进度键
     */
    private String getProgressKey(String fileMd5) {
        return UPLOAD_PROGRESS_KEY + fileMd5;
    }
    
    /**
     * 获取总分片数键
     */
    private String getTotalChunksKey(String fileMd5) {
        return UPLOAD_TOTAL_CHUNKS_KEY + fileMd5;
    }
    
    /**
     * 从Redis获取总分片数
     */
    private Integer getTotalChunksFromRedis(String fileMd5) {
        String totalChunksKey = getTotalChunksKey(fileMd5);
        Object totalChunks = redisTemplate.opsForValue().get(totalChunksKey);
        return totalChunks instanceof Integer ? (Integer) totalChunks : null;
    }
}