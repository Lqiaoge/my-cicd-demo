package com.windcore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 邮件消息实体类
 * 
 * @author windcore
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage {

    /**
     * 邮件ID
     */
    private String id;

    /**
     * 发送方邮箱
     */
    private String from;

    /**
     * 发送方名称
     */
    private String fromName;

    /**
     * 接收方邮箱列表
     */
    private List<String> to;

    /**
     * 抄送邮箱列表
     */
    private List<String> cc;

    /**
     * 密送邮箱列表
     */
    private List<String> bcc;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 是否HTML格式
     */
    private Boolean isHtml = true;

    /**
     * 邮件模板名称
     */
    private String templateName;

    /**
     * 模板变量
     */
    private Map<String, Object> templateVariables;

    /**
     * 附件列表
     */
    private List<MailAttachment> attachments;

    /**
     * 邮件优先级 (1-5, 1最高)
     */
    private Integer priority = 3;

    /**
     * 发送状态
     */
    private MailStatus status = MailStatus.PENDING;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 邮件状态枚举
     */
    public enum MailStatus {
        PENDING("待发送"),
        SENDING("发送中"),
        SUCCESS("发送成功"),
        FAILED("发送失败"),
        RETRY("重试中");

        private final String description;

        MailStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}