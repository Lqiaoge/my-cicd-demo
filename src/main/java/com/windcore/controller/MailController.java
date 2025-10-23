package com.windcore.controller;

import com.windcore.model.MailAttachment;
import com.windcore.model.MailMessage;
import com.windcore.service.MailQueueService;
import com.windcore.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 邮件发送控制器
 * 
 * @author windcore
 */
@Slf4j
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@Tag(name = "邮件服务", description = "提供邮件发送、状态查询、队列管理等功能")
public class MailController {

    private final MailService mailService;
    private final MailQueueService mailQueueService;

    /**
     * 发送简单文本邮件
     */
    @Operation(
        summary = "发送简单文本邮件",
        description = "发送纯文本格式的邮件，适用于简单的通知和消息发送",
        tags = {"邮件发送"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "邮件发送成功",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "成功响应",
                    value = """
                    {
                        "success": true,
                        "message": "简单邮件发送成功",
                        "messageId": "uuid-string",
                        "timestamp": "2024-01-01T12:00:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "邮件发送失败",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "失败响应",
                    value = """
                    {
                        "success": false,
                        "message": "发送失败: 邮箱地址格式错误",
                        "timestamp": "2024-01-01T12:00:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/send/simple")
    public ResponseEntity<Map<String, Object>> sendSimpleMail(
        @Parameter(description = "简单邮件请求参数", required = true)
        @RequestBody SimpleMailRequest request) {
        try {
            String messageId = mailService.sendSimpleMail(
                request.getTo(), 
                request.getSubject(), 
                request.getContent()
            );
            
            return ResponseEntity.ok(createSuccessResponse("简单邮件发送成功", messageId));
            
        } catch (Exception e) {
            log.error("发送简单邮件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("发送失败: " + e.getMessage()));
        }
    }

    /**
     * 发送HTML邮件
     */
    @Operation(
        summary = "发送HTML格式邮件",
        description = "发送富文本HTML格式的邮件，支持样式、图片、链接等HTML元素",
        tags = {"邮件发送"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "HTML邮件发送成功"),
        @ApiResponse(responseCode = "400", description = "HTML邮件发送失败")
    })
    @PostMapping("/send/html")
    public ResponseEntity<Map<String, Object>> sendHtmlMail(
        @Parameter(description = "HTML邮件请求参数", required = true)
        @RequestBody HtmlMailRequest request) {
        try {
            String messageId = mailService.sendHtmlMail(
                request.getTo(), 
                request.getSubject(), 
                request.getHtmlContent()
            );
            
            return ResponseEntity.ok(createSuccessResponse("HTML邮件发送成功", messageId));
            
        } catch (Exception e) {
            log.error("发送HTML邮件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("发送失败: " + e.getMessage()));
        }
    }

    /**
     * 发送模板邮件
     */
    @Operation(
        summary = "发送模板邮件",
        description = "使用预定义的邮件模板发送邮件，支持动态变量替换",
        tags = {"邮件发送"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "模板邮件发送成功"),
        @ApiResponse(responseCode = "400", description = "模板邮件发送失败")
    })
    @PostMapping("/send/template")
    public ResponseEntity<Map<String, Object>> sendTemplateMail(
        @Parameter(description = "模板邮件请求参数", required = true)
        @RequestBody TemplateMailRequest request) {
        try {
            String messageId = mailService.sendTemplateMail(
                request.getTo(), 
                request.getSubject(), 
                request.getTemplateName(), 
                request.getTemplateData()
            );
            
            return ResponseEntity.ok(createSuccessResponse("模板邮件发送成功", messageId));
            
        } catch (Exception e) {
            log.error("发送模板邮件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("发送失败: " + e.getMessage()));
        }
    }

    /**
     * 发送带附件的邮件
     */
    @Operation(
        summary = "发送带附件的邮件",
        description = "发送包含文件附件的邮件，支持多个附件上传",
        tags = {"邮件发送"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "带附件邮件发送成功"),
        @ApiResponse(responseCode = "400", description = "带附件邮件发送失败")
    })
    @PostMapping(value = "/send/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendMailWithAttachment(
            @Parameter(description = "收件人邮箱地址", required = true)
            @RequestParam("to") String to,
            @Parameter(description = "邮件主题", required = true)
            @RequestParam("subject") String subject,
            @Parameter(description = "邮件内容", required = true)
            @RequestParam("content") String content,
            @Parameter(description = "附件文件列表", required = true)
            @RequestParam("files") MultipartFile[] files) {
        
        try {
            List<MailAttachment> attachments = new ArrayList<>();
            
            // 处理附件
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    MailAttachment attachment = new MailAttachment();
                    attachment.setFileName(file.getOriginalFilename());
                    attachment.setContentType(file.getContentType());
                    attachment.setSize(file.getSize());
                    attachment.setContent(file.getBytes());
                    attachments.add(attachment);
                }
            }
            
            // 创建邮件消息
            MailMessage mailMessage = new MailMessage();
            mailMessage.setId(UUID.randomUUID().toString());
            mailMessage.setTo(Arrays.asList(to));
            mailMessage.setSubject(subject);
            mailMessage.setContent(content);
            mailMessage.setAttachments(attachments);
            mailMessage.setPriority(3);
            
            String messageId = mailService.sendMail(mailMessage);
            
            return ResponseEntity.ok(createSuccessResponse("带附件邮件发送成功", messageId));
            
        } catch (IOException e) {
            log.error("处理附件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("处理附件失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("发送带附件邮件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("发送失败: " + e.getMessage()));
        }
    }

    /**
     * 批量发送邮件
     */
    @PostMapping("/send/batch")
    public ResponseEntity<Map<String, Object>> sendBatchMail(@RequestBody BatchMailRequest request) {
        try {
            List<MailMessage> mailMessages = new ArrayList<>();
            for (String to : request.getToList()) {
                MailMessage mailMessage = new MailMessage();
                mailMessage.setId(UUID.randomUUID().toString());
                mailMessage.setTo(Arrays.asList(to));
                mailMessage.setSubject(request.getSubject());
                mailMessage.setContent(request.getContent());
                mailMessage.setPriority(3);
                mailMessage.setCreateTime(LocalDateTime.now());
                mailMessages.add(mailMessage);
            }
            
            List<String> messageIds = mailService.sendBatchMail(mailMessages);
            
            Map<String, Object> response = createSuccessResponse("批量邮件发送成功", null);
            response.put("messageIds", messageIds);
            response.put("count", messageIds.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量发送邮件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("批量发送失败: " + e.getMessage()));
        }
    }

    /**
     * 异步发送邮件（添加到队列）
     */
    @Operation(
        summary = "异步发送邮件",
        description = "将邮件添加到发送队列中进行异步处理，适用于大批量邮件发送",
        tags = {"邮件队列"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "邮件已成功添加到发送队列"),
        @ApiResponse(responseCode = "400", description = "添加邮件到队列失败")
    })
    @PostMapping("/send/async")
    public ResponseEntity<Map<String, Object>> sendAsyncMail(
        @Parameter(description = "异步邮件请求参数", required = true)
        @RequestBody AsyncMailRequest request) {
        try {
            MailMessage mailMessage = new MailMessage();
            mailMessage.setId(UUID.randomUUID().toString());
            mailMessage.setTo(Arrays.asList(request.getTo()));
            mailMessage.setCc(request.getCc() != null ? Arrays.asList(request.getCc()) : null);
            mailMessage.setBcc(request.getBcc() != null ? Arrays.asList(request.getBcc()) : null);
            mailMessage.setSubject(request.getSubject());
            mailMessage.setContent(request.getContent());
            mailMessage.setTemplateName(request.getTemplateName());
            mailMessage.setTemplateVariables(request.getTemplateData());
            mailMessage.setPriority(request.getPriority() != null ? request.getPriority() : 3);
            mailMessage.setMaxRetryCount(request.getMaxRetryCount() != null ? request.getMaxRetryCount() : 3);
            
            // 添加到队列
            mailQueueService.addToQueue(mailMessage);
            
            return ResponseEntity.ok(createSuccessResponse("邮件已添加到发送队列", mailMessage.getId()));
            
        } catch (Exception e) {
            log.error("添加邮件到队列失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("添加到队列失败: " + e.getMessage()));
        }
    }

    /**
     * 获取邮件状态
     */
    @Operation(
        summary = "获取邮件发送状态",
        description = "根据邮件ID查询邮件的当前发送状态",
        tags = {"邮件状态"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取邮件状态"),
        @ApiResponse(responseCode = "400", description = "获取邮件状态失败")
    })
    @GetMapping("/status/{messageId}")
    public ResponseEntity<Map<String, Object>> getMailStatus(
        @Parameter(description = "邮件消息ID", required = true, example = "uuid-string")
        @PathVariable String messageId) {
        try {
            MailMessage mailMessage = mailService.getMailStatus(messageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", messageId);
            response.put("status", mailMessage != null ? mailMessage.getStatus() : "NOT_FOUND");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取邮件状态失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("获取状态失败: " + e.getMessage()));
        }
    }

    /**
     * 获取队列状态
     */
    @GetMapping("/queue/status")
    public ResponseEntity<Map<String, Object>> getQueueStatus() {
        try {
            MailQueueService.QueueStatus queueStatus = mailQueueService.getQueueStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("queueStatus", queueStatus);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取队列状态失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("获取队列状态失败: " + e.getMessage()));
        }
    }

    /**
     * 重新发送失败的邮件
     */
    @Operation(
        summary = "重新发送失败的邮件",
        description = "重新发送之前发送失败的邮件",
        tags = {"邮件管理"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "邮件重新发送成功"),
        @ApiResponse(responseCode = "400", description = "邮件重新发送失败")
    })
    @PostMapping("/resend/{messageId}")
    public ResponseEntity<Map<String, Object>> resendMail(
        @Parameter(description = "邮件消息ID", required = true, example = "uuid-string")
        @PathVariable String messageId) {
        try {
            boolean success = mailService.resendMail(messageId);
            
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("邮件重新发送成功", messageId));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("邮件重新发送失败"));
            }
            
        } catch (Exception e) {
            log.error("重新发送邮件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("重新发送失败: " + e.getMessage()));
        }
    }

    /**
     * 取消待发送的邮件
     */
    @Operation(
        summary = "取消待发送的邮件",
        description = "取消队列中尚未发送的邮件",
        tags = {"邮件管理"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "邮件取消成功"),
        @ApiResponse(responseCode = "400", description = "邮件取消失败")
    })
    @PostMapping("/cancel/{messageId}")
    public ResponseEntity<Map<String, Object>> cancelMail(
        @Parameter(description = "邮件消息ID", required = true, example = "uuid-string")
        @PathVariable String messageId) {
        try {
            boolean success = mailService.cancelMail(messageId);
            
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("邮件取消成功", messageId));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("邮件取消失败"));
            }
            
        } catch (Exception e) {
            log.error("取消邮件失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("取消失败: " + e.getMessage()));
        }
    }

    /**
     * 测试邮件服务连接
     */
    @Operation(
        summary = "测试邮件服务连接",
        description = "测试邮件服务器连接是否正常",
        tags = {"邮件管理"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "邮件服务连接正常"),
        @ApiResponse(responseCode = "400", description = "邮件服务连接失败")
    })
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testMailService() {
        try {
            boolean isConnected = mailService.testConnection();
            
            if (isConnected) {
                return ResponseEntity.ok(createSuccessResponse("邮件服务连接正常", null));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("邮件服务连接失败"));
            }
            
        } catch (Exception e) {
            log.error("测试邮件服务连接失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("连接测试失败: " + e.getMessage()));
        }
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, String messageId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        if (messageId != null) {
            response.put("messageId", messageId);
        }
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    // 请求DTO类
    @Schema(description = "简单邮件请求参数")
    public static class SimpleMailRequest {
        @Schema(description = "收件人邮箱地址", example = "user@example.com", required = true)
        private String to;
        @Schema(description = "邮件主题", example = "测试邮件", required = true)
        private String subject;
        @Schema(description = "邮件内容", example = "这是一封测试邮件", required = true)
        private String content;
        
        // Getters and Setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    @Schema(description = "HTML邮件请求参数")
    public static class HtmlMailRequest {
        @Schema(description = "收件人邮箱地址", example = "user@example.com", required = true)
        private String to;
        @Schema(description = "邮件主题", example = "HTML测试邮件", required = true)
        private String subject;
        @Schema(description = "HTML邮件内容", example = "<h1>这是HTML邮件</h1><p>支持富文本格式</p>", required = true)
        private String htmlContent;
        
        // Getters and Setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getHtmlContent() { return htmlContent; }
        public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    }

    @Schema(description = "模板邮件请求参数")
    public static class TemplateMailRequest {
        @Schema(description = "收件人邮箱地址", example = "user@example.com", required = true)
        private String to;
        @Schema(description = "邮件主题", example = "模板邮件", required = true)
        private String subject;
        @Schema(description = "邮件模板名称", example = "welcome", required = true)
        private String templateName;
        @Schema(description = "模板变量数据", example = "{\"username\": \"张三\", \"code\": \"123456\"}")
        private Map<String, Object> templateData;
        
        // Getters and Setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public Map<String, Object> getTemplateData() { return templateData; }
        public void setTemplateData(Map<String, Object> templateData) { this.templateData = templateData; }
    }

    public static class BatchMailRequest {
        private List<String> toList;
        private String subject;
        private String content;
        
        // Getters and Setters
        public List<String> getToList() { return toList; }
        public void setToList(List<String> toList) { this.toList = toList; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    @Schema(description = "异步邮件请求参数")
    public static class AsyncMailRequest {
        @Schema(description = "收件人邮箱地址", example = "user@example.com", required = true)
        private String to;
        @Schema(description = "抄送邮箱地址", example = "cc@example.com")
        private String cc;
        @Schema(description = "密送邮箱地址", example = "bcc@example.com")
        private String bcc;
        @Schema(description = "邮件主题", example = "异步邮件", required = true)
        private String subject;
        @Schema(description = "邮件内容", example = "这是一封异步发送的邮件")
        private String content;
        @Schema(description = "邮件模板名称", example = "notification")
        private String templateName;
        @Schema(description = "模板变量数据")
        private Map<String, Object> templateData;
        @Schema(description = "邮件优先级 (1-5, 1最高)", example = "3", minimum = "1", maximum = "5")
        private Integer priority;
        @Schema(description = "最大重试次数", example = "3", minimum = "0")
        private Integer maxRetryCount;
        
        // Getters and Setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getCc() { return cc; }
        public void setCc(String cc) { this.cc = cc; }
        public String getBcc() { return bcc; }
        public void setBcc(String bcc) { this.bcc = bcc; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public Map<String, Object> getTemplateData() { return templateData; }
        public void setTemplateData(Map<String, Object> templateData) { this.templateData = templateData; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public Integer getMaxRetryCount() { return maxRetryCount; }
        public void setMaxRetryCount(Integer maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    }
}