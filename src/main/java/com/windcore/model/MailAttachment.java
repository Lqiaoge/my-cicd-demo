package com.windcore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

/**
 * 邮件附件实体类
 * 
 * @author windcore
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailAttachment {

    /**
     * 附件名称
     */
    private String fileName;

    /**
     * 附件内容类型
     */
    private String contentType;

    /**
     * 附件大小（字节）
     */
    private Long size;

    /**
     * 附件文件路径
     */
    private String filePath;

    /**
     * 附件输入流
     */
    private InputStream inputStream;

    /**
     * 附件字节数组
     */
    private byte[] content;

    /**
     * 是否为内联附件（用于嵌入图片等）
     */
    private Boolean inline = false;

    /**
     * 内联附件的Content-ID
     */
    private String contentId;
}