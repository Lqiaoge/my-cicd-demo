package com.windcore.dto;

import com.windcore.model.FileMetadata;

/**
 * 文件上传结果DTO
 */
public class FileUploadResult {
    private boolean success;
    private String message;
    private FileMetadata metadata;

    private FileUploadResult(boolean success, String message, FileMetadata metadata) {
        this.success = success;
        this.message = message;
        this.metadata = metadata;
    }

    /**
     * 创建成功结果
     * @param metadata 文件元数据
     * @return 成功结果
     */
    public static FileUploadResult success(FileMetadata metadata) {
        return new FileUploadResult(true, "文件上传成功", metadata);
    }

    /**
     * 创建失败结果
     * @param message 失败消息
     * @return 失败结果
     */
    public static FileUploadResult failure(String message) {
        return new FileUploadResult(false, message, null);
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FileMetadata metadata) {
        this.metadata = metadata;
    }
}