package com.windcore.controller;

import com.windcore.dto.*;
import com.windcore.exception.FileUploadException;
import com.windcore.model.FileMetadata;
import com.windcore.service.ChunkFileUploadService;
import com.windcore.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "文件服务", description = "提供文件分片上传、断点续传、文件管理等功能")
public class FileUploadController {
    
    @Autowired
    private ChunkFileUploadService chunkUploadService;
    
    @Autowired
    private FileService fileService;
    
    /**
     * 分片上传
     */
    @Operation(
        summary = "分片文件上传",
        description = "上传文件分片，支持大文件分片上传和断点续传",
        tags = {"文件上传"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分片上传成功"),
        @ApiResponse(responseCode = "400", description = "分片上传失败")
    })
    @PostMapping("/chunk")
    public ResponseEntity<ChunkUploadResult> uploadChunk(
        @Parameter(description = "文件分片信息", required = true)
        @ModelAttribute FileChunk chunk) {
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
    @Operation(
        summary = "获取文件上传进度",
        description = "根据文件MD5查询当前上传进度",
        tags = {"文件上传"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取上传进度"),
        @ApiResponse(responseCode = "500", description = "查询上传进度失败")
    })
    @GetMapping("/upload-progress/{fileMd5}")
    public ResponseEntity<UploadProgress> getUploadProgress(
        @Parameter(description = "文件MD5值", required = true, example = "d41d8cd98f00b204e9800998ecf8427e")
        @PathVariable String fileMd5) {
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
    @Operation(
        summary = "文件验证",
        description = "验证文件是否已存在，支持秒传和断点续传功能",
        tags = {"文件验证"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文件验证成功"),
        @ApiResponse(responseCode = "500", description = "文件验证失败")
    })
    @PostMapping("/verify")
    public ResponseEntity<FileVerifyResult> verifyFile(
        @Parameter(description = "文件验证请求参数", required = true)
        @RequestBody FileVerifyRequest request) {
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
    @Operation(
        summary = "获取已上传分片列表",
        description = "根据文件MD5获取已成功上传的分片编号列表",
        tags = {"文件上传"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取已上传分片列表"),
        @ApiResponse(responseCode = "500", description = "查询已上传分片失败")
    })
    @GetMapping("/uploaded-chunks/{fileMd5}")
    public ResponseEntity<List<Integer>> getUploadedChunks(
        @Parameter(description = "文件MD5值", required = true, example = "d41d8cd98f00b204e9800998ecf8427e")
        @PathVariable String fileMd5) {
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
    @Operation(
        summary = "查询文件信息",
        description = "根据文件MD5查询文件的详细信息",
        tags = {"文件查询"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取文件信息"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "500", description = "查询文件信息失败")
    })
    @GetMapping("/info/{fileMd5}")
    public ResponseEntity<FileMetadata> getFileInfo(
        @Parameter(description = "文件MD5值", required = true, example = "d41d8cd98f00b204e9800998ecf8427e")
        @PathVariable String fileMd5) {
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
    @Operation(
        summary = "查询文件列表",
        description = "根据业务类型或文件名查询文件列表",
        tags = {"文件查询"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取文件列表"),
        @ApiResponse(responseCode = "500", description = "查询文件列表失败")
    })
    @GetMapping("/list")
    public ResponseEntity<List<FileMetadata>> getFileList(
            @Parameter(description = "业务类型", required = false, example = "avatar")
            @RequestParam(required = false) String businessType,
            @Parameter(description = "文件名关键字", required = false, example = "test.jpg")
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
    @Operation(
        summary = "删除文件",
        description = "根据文件ID删除文件",
        tags = {"文件管理"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文件删除成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "500", description = "删除文件失败")
    })
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(
        @Parameter(description = "文件ID", required = true, example = "123456")
        @PathVariable String fileId) {
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
    @Operation(
        summary = "获取文件统计信息",
        description = "获取系统中文件的统计信息",
        tags = {"文件统计"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取文件统计信息"),
        @ApiResponse(responseCode = "500", description = "获取文件统计信息失败")
    })
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