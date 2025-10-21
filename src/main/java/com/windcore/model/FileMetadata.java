package com.windcore.model;

import java.time.LocalDateTime;

/**
 * 文件元数据模型
 */
public class FileMetadata {
    private String id;
    private String fileName;
    private String safeFileName;
    private String filePath;
    private String fileType;
    private long fileSize;
    private String businessType;
    private LocalDateTime uploadTime;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSafeFileName() {
        return safeFileName;
    }

    public void setSafeFileName(String safeFileName) {
        this.safeFileName = safeFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", safeFileName='" + safeFileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", businessType='" + businessType + '\'' +
                ", uploadTime=" + uploadTime +
                '}';
    }
}