package com.windcore.dto;

import com.windcore.model.FileMetadata;
import lombok.Getter;
import lombok.Setter;

/**
 * 分片上传结果
 * 
 * @author windcore
 */
@Setter
@Getter
public class ChunkUploadResult {
    
    /**
     * 上传状态
     */
    public enum Status {
        IN_PROGRESS,    // 上传中
        COMPLETED,      // 上传完成
        FAILED          // 上传失败
    }

    // Getters and Setters
    /**
     * 状态
     */
    private Status status;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 当前分片号
     */
    private Integer currentChunk;
    
    /**
     * 文件元数据（上传完成时）
     */
    private FileMetadata fileMetadata;
    
    /**
     * 上传进度（百分比）
     */
    private Double progress;
    
    // 构造函数
    public ChunkUploadResult() {}
    
    public ChunkUploadResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // 静态工厂方法
    public static ChunkUploadResult inProgress(Integer currentChunk) {
        ChunkUploadResult result = new ChunkUploadResult();
        result.status = Status.IN_PROGRESS;
        result.message = "分片上传中";
        result.currentChunk = currentChunk;
        return result;
    }
    
    public static ChunkUploadResult inProgress(Integer currentChunk, Double progress) {
        ChunkUploadResult result = inProgress(currentChunk);
        result.progress = progress;
        return result;
    }
    
    public static ChunkUploadResult completed(FileMetadata fileMetadata) {
        ChunkUploadResult result = new ChunkUploadResult();
        result.status = Status.COMPLETED;
        result.message = "文件上传完成";
        result.fileMetadata = fileMetadata;
        result.progress = 100.0;
        return result;
    }
    
    public static ChunkUploadResult failed(String message) {
        ChunkUploadResult result = new ChunkUploadResult();
        result.status = Status.FAILED;
        result.message = message;
        return result;
    }

    @Override
    public String toString() {
        return "ChunkUploadResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", currentChunk=" + currentChunk +
                ", fileMetadata=" + fileMetadata +
                ", progress=" + progress +
                '}';
    }
}