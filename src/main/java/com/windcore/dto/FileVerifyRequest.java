package com.windcore.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 文件验证请求
 * 
 * @author windcore
 */
@Setter
@Getter
public class FileVerifyRequest {

    // Getters and Setters
    /**
     * 文件MD5值
     */
    private String fileMd5;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 分片大小
     */
    private Long chunkSize;
    
    /**
     * 分片总数
     */
    private Integer totalChunks;
    
    // 构造函数
    public FileVerifyRequest() {}
    
    public FileVerifyRequest(String fileMd5, String fileName, Long fileSize) {
        this.fileMd5 = fileMd5;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "FileVerifyRequest{" +
                "fileMd5='" + fileMd5 + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", businessType='" + businessType + '\'' +
                ", chunkSize=" + chunkSize +
                ", totalChunks=" + totalChunks +
                '}';
    }
}