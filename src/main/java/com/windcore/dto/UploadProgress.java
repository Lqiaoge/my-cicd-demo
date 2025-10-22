package com.windcore.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 上传进度信息
 * 
 * @author windcore
 */
@Setter
@Getter
public class UploadProgress {

    // Getters and Setters
    /**
     * 已上传的分片数
     */
    private Integer uploadedChunks;
    
    /**
     * 总分片数
     */
    private Integer totalChunks;
    
    /**
     * 上传进度（百分比）
     */
    private Double progress;
    
    /**
     * 已上传的字节数
     */
    private Long uploadedBytes;
    
    /**
     * 文件总字节数
     */
    private Long totalBytes;
    
    /**
     * 上传状态
     */
    private String status;
    
    /**
     * 文件MD5
     */
    private String fileMd5;
    
    // 构造函数
    public UploadProgress() {}
    
    public UploadProgress(Integer uploadedChunks, Integer totalChunks, Double progress) {
        this.uploadedChunks = uploadedChunks;
        this.totalChunks = totalChunks;
        this.progress = progress;
    }
    
    // Builder模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UploadProgress progress = new UploadProgress();
        
        public Builder uploadedChunks(Integer uploadedChunks) {
            progress.uploadedChunks = uploadedChunks;
            return this;
        }
        
        public Builder totalChunks(Integer totalChunks) {
            progress.totalChunks = totalChunks;
            return this;
        }
        
        public Builder progress(Double progressValue) {
            progress.progress = progressValue;
            return this;
        }
        
        public Builder uploadedBytes(Long uploadedBytes) {
            progress.uploadedBytes = uploadedBytes;
            return this;
        }
        
        public Builder totalBytes(Long totalBytes) {
            progress.totalBytes = totalBytes;
            return this;
        }
        
        public Builder status(String status) {
            progress.status = status;
            return this;
        }
        
        public Builder fileMd5(String fileMd5) {
            progress.fileMd5 = fileMd5;
            return this;
        }
        
        public UploadProgress build() {
            return progress;
        }
    }

    @Override
    public String toString() {
        return "UploadProgress{" +
                "uploadedChunks=" + uploadedChunks +
                ", totalChunks=" + totalChunks +
                ", progress=" + progress +
                ", uploadedBytes=" + uploadedBytes +
                ", totalBytes=" + totalBytes +
                ", status='" + status + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                '}';
    }
}