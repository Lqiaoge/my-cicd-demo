package com.windcore.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件分片数据传输对象
 * 
 * @author windcore
 */
@Setter
@Getter
public class FileChunk {

    // Getters and Setters
    /**
     * 分片文件
     */
    private MultipartFile file;
    
    /**
     * 分片序号（从0开始）
     */
    private Integer chunkNumber;
    
    /**
     * 分片总数
     */
    private Integer totalChunks;
    
    /**
     * 分片大小
     */
    private Long chunkSize;
    
    /**
     * 文件总大小
     */
    private Long totalSize;
    
    /**
     * 文件MD5值
     */
    private String fileMd5;
    
    /**
     * 分片MD5值
     */
    private String chunkMd5;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    // 构造函数
    public FileChunk() {}
    
    public FileChunk(MultipartFile file, Integer chunkNumber, Integer totalChunks, 
                     String fileMd5, String chunkMd5, String fileName) {
        this.file = file;
        this.chunkNumber = chunkNumber;
        this.totalChunks = totalChunks;
        this.fileMd5 = fileMd5;
        this.chunkMd5 = chunkMd5;
        this.fileName = fileName;
    }
    
    /**
     * 获取分片数据
     */
    public byte[] getData() {
        try {
            return file != null ? file.getBytes() : new byte[0];
        } catch (Exception e) {
            throw new RuntimeException("获取分片数据失败", e);
        }
    }

    @Override
    public String toString() {
        return "FileChunk{" +
                "chunkNumber=" + chunkNumber +
                ", totalChunks=" + totalChunks +
                ", chunkSize=" + chunkSize +
                ", totalSize=" + totalSize +
                ", fileMd5='" + fileMd5 + '\'' +
                ", chunkMd5='" + chunkMd5 + '\'' +
                ", fileName='" + fileName + '\'' +
                ", businessType='" + businessType + '\'' +
                '}';
    }
}