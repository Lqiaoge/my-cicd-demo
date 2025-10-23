package com.windcore.controller;

import com.windcore.model.MailAttachment;
import com.windcore.model.MailMessage;
import com.windcore.service.MailQueueService;
import com.windcore.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MailController {

    private final MailService mailService;
    private final MailQueueService mailQueueService;

    /**
     * 发送简单文本邮件
     */
    @PostMapping("/send/simple")
    public ResponseEntity<Map<String, Object>> sendSimpleMail(@RequestBody SimpleMailRequest request) {
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
    @PostMapping("/send/html")
    public ResponseEntity<Map<String, Object>> sendHtmlMail(@RequestBody HtmlMailRequest request) {
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
    @PostMapping("/send/template")
    public ResponseEntity<Map<String, Object>> sendTemplateMail(@RequestBody TemplateMailRequest request) {
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
    @PostMapping("/send/attachment")
    public ResponseEntity<Map<String, Object>> sendMailWithAttachment(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("content") String content,
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
    @PostMapping("/send/async")
    public ResponseEntity<Map<String, Object>> sendAsyncMail(@RequestBody AsyncMailRequest request) {
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
    @GetMapping("/status/{messageId}")
    public ResponseEntity<Map<String, Object>> getMailStatus(@PathVariable String messageId) {
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
    @PostMapping("/resend/{messageId}")
    public ResponseEntity<Map<String, Object>> resendMail(@PathVariable String messageId) {
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
    @PostMapping("/cancel/{messageId}")
    public ResponseEntity<Map<String, Object>> cancelMail(@PathVariable String messageId) {
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
    public static class SimpleMailRequest {
        private String to;
        private String subject;
        private String content;
        
        // Getters and Setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class HtmlMailRequest {
        private String to;
        private String subject;
        private String htmlContent;
        
        // Getters and Setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getHtmlContent() { return htmlContent; }
        public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    }

    public static class TemplateMailRequest {
        private String to;
        private String subject;
        private String templateName;
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

    public static class AsyncMailRequest {
        private String to;
        private String cc;
        private String bcc;
        private String subject;
        private String content;
        private String templateName;
        private Map<String, Object> templateData;
        private Integer priority;
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