package com.windcore.dto;

import com.windcore.model.FileMetadata;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件验证结果
 * 
 * @author windcore
 */
@Setter
@Getter
public class FileVerifyResult {
    
    /**
     * 验证状态
     */
    public enum Status {
        EXISTING,       // 文件已存在（秒传）
        RESUME,         // 可以断点续传
        NEW_UPLOAD      // 需要重新上传
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
     * 文件元数据（文件已存在时）
     */
    private FileMetadata fileMetadata;
    
    /**
     * 上传进度（断点续传时）
     */
    private UploadProgress uploadProgress;
    
    /**
     * 是否需要上传
     */
    private Boolean needUpload;
    
    /**
     * 已上传的分片列表
     */
    private java.util.List<Integer> uploadedChunks;
    
    // 构造函数
    public FileVerifyResult() {}
    
    public FileVerifyResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // 静态工厂方法
    public static FileVerifyResult existing(FileMetadata fileMetadata) {
        FileVerifyResult result = new FileVerifyResult();
        result.status = Status.EXISTING;
        result.message = "文件已存在，支持秒传";
        result.fileMetadata = fileMetadata;
        result.needUpload = false;
        return result;
    }
    
    public static FileVerifyResult resume(UploadProgress uploadProgress) {
        FileVerifyResult result = new FileVerifyResult();
        result.status = Status.RESUME;
        result.message = "支持断点续传";
        result.uploadProgress = uploadProgress;
        result.needUpload = true;
        return result;
    }
    
    public static FileVerifyResult resume(UploadProgress uploadProgress, java.util.List<Integer> uploadedChunks) {
        FileVerifyResult result = resume(uploadProgress);
        result.uploadedChunks = uploadedChunks;
        return result;
    }
    
    public static FileVerifyResult newUpload() {
        FileVerifyResult result = new FileVerifyResult();
        result.status = Status.NEW_UPLOAD;
        result.message = "需要重新上传";
        result.needUpload = true;
        return result;
    }
    
    public static FileVerifyResult newUpload(String message) {
        FileVerifyResult result = new FileVerifyResult();
        result.status = Status.NEW_UPLOAD;
        result.message = message;
        result.needUpload = true;
        return result;
    }

    @Override
    public String toString() {
        return "FileVerifyResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", fileMetadata=" + fileMetadata +
                ", uploadProgress=" + uploadProgress +
                ", needUpload=" + needUpload +
                ", uploadedChunks=" + uploadedChunks +
                '}';
    }
}