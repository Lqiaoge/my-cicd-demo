package com.windcore.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 文件元数据模型
 */
@Setter
@Getter
public class FileMetadata {
    // Getters and setters
    private String id;
    private String fileName;
    private String safeFileName;
    private String filePath;
    private String fileType;
    private long fileSize;
    private String fileMd5;
    private String businessType;
    private LocalDateTime uploadTime;

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", safeFileName='" + safeFileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", fileMd5='" + fileMd5 + '\'' +
                ", businessType='" + businessType + '\'' +
                ", uploadTime=" + uploadTime +
                '}';
    }
}