package com.windcore.controller;

import com.windcore.dto.*;
import com.windcore.exception.FileUploadException;
import com.windcore.model.FileMetadata;
import com.windcore.service.ChunkFileUploadService;
import com.windcore.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件上传控制器
 * 
 * @author windcore
 */
@RestController
@RequestMapping("/api/file")
@Slf4j
public class FileUploadController {
    
    @Autowired
    private ChunkFileUploadService chunkUploadService;
    
    @Autowired
    private FileService fileService;
    
    /**
     * 分片上传
     */
    @PostMapping("/chunk")
    public ResponseEntity<ChunkUploadResult> uploadChunk(@ModelAttribute FileChunk chunk) {
        try {
            log.info("接收分片上传请求: 文件MD5={}, 分片号={}/{}, 文件名={}", 
                    chunk.getFileMd5(), chunk.getChunkNumber(), chunk.getTotalChunks(), chunk.getFileName());
            
            ChunkUploadResult result = chunkUploadService.uploadChunk(chunk);
            
            log.info("分片上传处理完成: 文件MD5={}, 状态={}", chunk.getFileMd5(), result.getStatus());
            
            return ResponseEntity.ok(result);
        } catch (FileUploadException e) {
            log.error("分片上传失败: 文件MD5=" + chunk.getFileMd5(), e);
            return ResponseEntity.badRequest().body(ChunkUploadResult.failed(e.getMessage()));
        } catch (Exception e) {
            log.error("分片上传异常: 文件MD5=" + chunk.getFileMd5(), e);
            return ResponseEntity.internalServerError().body(ChunkUploadResult.failed("系统异常，请稍后重试"));
        }
    }
    
    /**
     * 获取上传进度
     */
    @GetMapping("/upload-progress/{fileMd5}")
    public ResponseEntity<UploadProgress> getUploadProgress(@PathVariable String fileMd5) {
        try {
            log.info("查询上传进度: 文件MD5={}", fileMd5);
            
            UploadProgress progress = chunkUploadService.getUploadProgress(fileMd5);
            
            log.info("上传进度查询完成: 文件MD5={}, 进度={}%", fileMd5, progress.getProgress());
            
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            log.error("查询上传进度失败: 文件MD5=" + fileMd5, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 文件验证（秒传功能）
     */
    @PostMapping("/verify")
    public ResponseEntity<FileVerifyResult> verifyFile(@RequestBody FileVerifyRequest request) {
        try {
            log.info("文件验证请求: 文件MD5={}, 文件名={}, 大小={}", 
                    request.getFileMd5(), request.getFileName(), request.getFileSize());
            
            // 检查文件是否已存在（秒传功能）
            FileMetadata existingFile = fileService.findByMd5(request.getFileMd5());
            if (existingFile != null) {
                log.info("文件已存在，支持秒传: 文件MD5={}, 文件路径={}", 
                        request.getFileMd5(), existingFile.getFilePath());
                return ResponseEntity.ok(FileVerifyResult.existing(existingFile));
            }
            
            // 检查上传进度（断点续传）
            UploadProgress progress = chunkUploadService.getUploadProgress(request.getFileMd5());
            if (progress.getUploadedChunks() > 0) {
                log.info("支持断点续传: 文件MD5={}, 已上传分片={}/{}", 
                        request.getFileMd5(), progress.getUploadedChunks(), progress.getTotalChunks());
                
                // 获取已上传的分片列表
                List<Integer> uploadedChunks = chunkUploadService.getUploadedChunks(request.getFileMd5());
                return ResponseEntity.ok(FileVerifyResult.resume(progress, uploadedChunks));
            }
            
            // 需要重新上传
            log.info("需要重新上传: 文件MD5={}", request.getFileMd5());
            return ResponseEntity.ok(FileVerifyResult.newUpload("文件不存在，需要上传"));
            
        } catch (Exception e) {
            log.error("文件验证失败: 文件MD5=" + request.getFileMd5(), e);
            return ResponseEntity.internalServerError().body(
                    FileVerifyResult.newUpload("验证失败，请重新上传"));
        }
    }
    
    /**
     * 获取已上传的分片列表
     */
    @GetMapping("/uploaded-chunks/{fileMd5}")
    public ResponseEntity<List<Integer>> getUploadedChunks(@PathVariable String fileMd5) {
        try {
            log.info("查询已上传分片: 文件MD5={}", fileMd5);
            
            List<Integer> uploadedChunks = chunkUploadService.getUploadedChunks(fileMd5);
            
            log.info("已上传分片查询完成: 文件MD5={}, 分片数={}", fileMd5, uploadedChunks.size());
            
            return ResponseEntity.ok(uploadedChunks);
        } catch (Exception e) {
            log.error("查询已上传分片失败: 文件MD5=" + fileMd5, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据MD5查询文件信息
     */
    @GetMapping("/info/{fileMd5}")
    public ResponseEntity<FileMetadata> getFileInfo(@PathVariable String fileMd5) {
        try {
            log.info("查询文件信息: 文件MD5={}", fileMd5);
            
            FileMetadata fileMetadata = fileService.findByMd5(fileMd5);
            if (fileMetadata == null) {
                log.info("文件不存在: 文件MD5={}", fileMd5);
                return ResponseEntity.notFound().build();
            }
            
            log.info("文件信息查询完成: 文件MD5={}, 文件名={}", fileMd5, fileMetadata.getFileName());
            
            return ResponseEntity.ok(fileMetadata);
        } catch (Exception e) {
            log.error("查询文件信息失败: 文件MD5=" + fileMd5, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据业务类型查询文件列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileMetadata>> getFileList(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String fileName) {
        try {
            List<FileMetadata> fileList;
            
            if (businessType != null && !businessType.trim().isEmpty()) {
                log.info("按业务类型查询文件列表: 业务类型={}", businessType);
                fileList = fileService.findByBusinessType(businessType);
            } else if (fileName != null && !fileName.trim().isEmpty()) {
                log.info("按文件名查询文件列表: 文件名关键字={}", fileName);
                fileList = fileService.findByFileNameLike(fileName);
            } else {
                log.info("查询所有文件列表");
                fileList = fileService.findByBusinessType(""); // 返回空列表，避免返回所有文件
            }
            
            log.info("文件列表查询完成: 文件数量={}", fileList.size());
            
            return ResponseEntity.ok(fileList);
        } catch (Exception e) {
            log.error("查询文件列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId) {
        try {
            log.info("删除文件请求: 文件ID={}", fileId);
            
            boolean deleted = fileService.deleteById(fileId);
            if (deleted) {
                log.info("文件删除成功: 文件ID={}", fileId);
                return ResponseEntity.ok("文件删除成功");
            } else {
                log.warn("文件不存在或删除失败: 文件ID={}", fileId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除文件失败: 文件ID=" + fileId, e);
            return ResponseEntity.internalServerError().body("删除文件失败");
        }
    }
    
    /**
     * 获取文件统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<FileStats> getFileStats() {
        try {
            log.info("查询文件统计信息");
            
            long totalFiles = fileService.count();
            
            FileStats stats = new FileStats();
            stats.setTotalFiles(totalFiles);
            
            log.info("文件统计信息查询完成: 总文件数={}", totalFiles);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("查询文件统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 文件统计信息DTO
     */
    public static class FileStats {
        private Long totalFiles;
        
        public Long getTotalFiles() {
            return totalFiles;
        }
        
        public void setTotalFiles(Long totalFiles) {
            this.totalFiles = totalFiles;
        }
    }
}