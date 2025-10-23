package com.windcore.service.impl;

import com.windcore.config.MailProperties;
import com.windcore.model.MailAttachment;
import com.windcore.model.MailMessage;
import com.windcore.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件服务实现类
 * 
 * @author windcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;

    /**
     * 邮件存储（实际项目中应使用数据库）
     */
    private final Map<String, MailMessage> mailStore = new ConcurrentHashMap<>();

    @Override
    public String sendSimpleMail(String to, String subject, String content) {
        return sendSimpleMail(Collections.singletonList(to), subject, content);
    }

    @Override
    public String sendSimpleMail(List<String> toList, String subject, String content) {
        MailMessage mailMessage = MailMessage.builder()
                .id(generateMailId())
                .from(mailProperties.getSender().getFrom())
                .fromName(mailProperties.getSender().getName())
                .to(toList)
                .subject(subject)
                .content(content)
                .isHtml(false)
                .createTime(LocalDateTime.now())
                .build();

        return sendMail(mailMessage);
    }

    @Override
    public String sendHtmlMail(String to, String subject, String htmlContent) {
        return sendHtmlMail(Collections.singletonList(to), subject, htmlContent);
    }

    @Override
    public String sendHtmlMail(List<String> toList, String subject, String htmlContent) {
        MailMessage mailMessage = MailMessage.builder()
                .id(generateMailId())
                .from(mailProperties.getSender().getFrom())
                .fromName(mailProperties.getSender().getName())
                .to(toList)
                .subject(subject)
                .content(htmlContent)
                .isHtml(true)
                .createTime(LocalDateTime.now())
                .build();

        return sendMail(mailMessage);
    }

    @Override
    public String sendTemplateMail(String to, String subject, String templateName, Map<String, Object> variables) {
        return sendTemplateMail(Collections.singletonList(to), subject, templateName, variables);
    }

    @Override
    public String sendTemplateMail(List<String> toList, String subject, String templateName, Map<String, Object> variables) {
        MailMessage mailMessage = MailMessage.builder()
                .id(generateMailId())
                .from(mailProperties.getSender().getFrom())
                .fromName(mailProperties.getSender().getName())
                .to(toList)
                .subject(subject)
                .templateName(templateName)
                .templateVariables(variables)
                .isHtml(true)
                .createTime(LocalDateTime.now())
                .build();

        return sendMail(mailMessage);
    }

    @Override
    public String sendMail(MailMessage mailMessage) {
        try {
            // 保存邮件信息
            mailStore.put(mailMessage.getId(), mailMessage);
            
            // 更新状态为发送中
            mailMessage.setStatus(MailMessage.MailStatus.SENDING);
            
            if (mailMessage.getIsHtml() || StringUtils.hasText(mailMessage.getTemplateName())) {
                sendMimeMessage(mailMessage);
            } else {
                sendSimpleMessage(mailMessage);
            }
            
            // 更新状态为发送成功
            mailMessage.setStatus(MailMessage.MailStatus.SUCCESS);
            mailMessage.setSendTime(LocalDateTime.now());
            
            log.info("邮件发送成功，邮件ID: {}, 收件人: {}", mailMessage.getId(), mailMessage.getTo());
            return mailMessage.getId();
            
        } catch (Exception e) {
            log.error("邮件发送失败，邮件ID: {}, 错误信息: {}", mailMessage.getId(), e.getMessage(), e);
            
            // 更新状态为发送失败
            mailMessage.setStatus(MailMessage.MailStatus.FAILED);
            mailMessage.setErrorMessage(e.getMessage());
            
            // 如果重试次数未达到上限，标记为重试
            if (mailMessage.getRetryCount() < mailMessage.getMaxRetryCount()) {
                mailMessage.setRetryCount(mailMessage.getRetryCount() + 1);
                mailMessage.setStatus(MailMessage.MailStatus.RETRY);
            }
            
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Async("mailTaskExecutor")
    public CompletableFuture<String> sendMailAsync(MailMessage mailMessage) {
        try {
            String mailId = sendMail(mailMessage);
            return CompletableFuture.completedFuture(mailId);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public List<String> sendBatchMail(List<MailMessage> mailMessages) {
        List<String> results = new ArrayList<>();
        for (MailMessage mailMessage : mailMessages) {
            try {
                String mailId = sendMail(mailMessage);
                results.add(mailId);
            } catch (Exception e) {
                log.error("批量发送邮件失败，邮件ID: {}", mailMessage.getId(), e);
                results.add(null);
            }
        }
        return results;
    }

    @Override
    @Async("mailTaskExecutor")
    public CompletableFuture<List<String>> sendBatchMailAsync(List<MailMessage> mailMessages) {
        try {
            List<String> results = sendBatchMail(mailMessages);
            return CompletableFuture.completedFuture(results);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public MailMessage getMailStatus(String mailId) {
        return mailStore.get(mailId);
    }

    @Override
    public boolean resendMail(String mailId) {
        MailMessage mailMessage = mailStore.get(mailId);
        if (mailMessage == null) {
            return false;
        }
        
        try {
            // 重置状态
            mailMessage.setStatus(MailMessage.MailStatus.PENDING);
            mailMessage.setErrorMessage(null);
            
            sendMail(mailMessage);
            return true;
        } catch (Exception e) {
            log.error("重新发送邮件失败，邮件ID: {}", mailId, e);
            return false;
        }
    }

    @Override
    public boolean cancelMail(String mailId) {
        MailMessage mailMessage = mailStore.get(mailId);
        if (mailMessage == null || mailMessage.getStatus() != MailMessage.MailStatus.PENDING) {
            return false;
        }
        
        mailStore.remove(mailId);
        return true;
    }

    @Override
    public List<MailMessage> getMailHistory(int page, int size) {
        return mailStore.values().stream()
                .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public boolean testConnection() {
        try {
            javaMailSender.createMimeMessage();
            return true;
        } catch (Exception e) {
            log.error("邮件服务连接测试失败", e);
            return false;
        }
    }

    /**
     * 发送简单文本邮件
     */
    private void sendSimpleMessage(MailMessage mailMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailMessage.getFrom());
        message.setTo(mailMessage.getTo().toArray(new String[0]));
        
        if (!CollectionUtils.isEmpty(mailMessage.getCc())) {
            message.setCc(mailMessage.getCc().toArray(new String[0]));
        }
        
        if (!CollectionUtils.isEmpty(mailMessage.getBcc())) {
            message.setBcc(mailMessage.getBcc().toArray(new String[0]));
        }
        
        message.setSubject(mailMessage.getSubject());
        message.setText(mailMessage.getContent());
        
        javaMailSender.send(message);
    }

    /**
     * 发送MIME邮件（支持HTML和附件）
     */
    private void sendMimeMessage(MailMessage mailMessage) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        // 设置发送方
        if (StringUtils.hasText(mailMessage.getFromName())) {
            helper.setFrom(mailMessage.getFrom(), mailMessage.getFromName());
        } else {
            helper.setFrom(mailMessage.getFrom());
        }
        
        // 设置收件人
        helper.setTo(mailMessage.getTo().toArray(new String[0]));
        
        if (!CollectionUtils.isEmpty(mailMessage.getCc())) {
            helper.setCc(mailMessage.getCc().toArray(new String[0]));
        }
        
        if (!CollectionUtils.isEmpty(mailMessage.getBcc())) {
            helper.setBcc(mailMessage.getBcc().toArray(new String[0]));
        }
        
        // 设置主题
        helper.setSubject(mailMessage.getSubject());
        
        // 设置内容
        String content = getMailContent(mailMessage);
        helper.setText(content, mailMessage.getIsHtml());
        
        // 设置优先级
        if (mailMessage.getPriority() != null) {
            mimeMessage.setHeader("X-Priority", String.valueOf(mailMessage.getPriority()));
        }
        
        // 添加附件
        addAttachments(helper, mailMessage.getAttachments());
        
        javaMailSender.send(mimeMessage);
    }

    /**
     * 获取邮件内容
     */
    private String getMailContent(MailMessage mailMessage) {
        if (StringUtils.hasText(mailMessage.getTemplateName())) {
            // 使用模板生成内容
            Context context = new Context();
            if (mailMessage.getTemplateVariables() != null) {
                context.setVariables(mailMessage.getTemplateVariables());
            }
            return templateEngine.process(mailMessage.getTemplateName(), context);
        } else {
            // 直接使用内容
            return mailMessage.getContent();
        }
    }

    /**
     * 添加附件
     */
    private void addAttachments(MimeMessageHelper helper, List<MailAttachment> attachments) throws MessagingException {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        
        for (MailAttachment attachment : attachments) {
            if (StringUtils.hasText(attachment.getFilePath())) {
                // 文件路径附件
                helper.addAttachment(attachment.getFileName(), new File(attachment.getFilePath()));
            } else if (attachment.getContent() != null) {
                // 字节数组附件
                helper.addAttachment(attachment.getFileName(), new ByteArrayResource(attachment.getContent()));
            } else if (attachment.getInputStream() != null) {
                // 输入流附件
                helper.addAttachment(attachment.getFileName(), new InputStreamResource(attachment.getInputStream()));
            }
            
            // 处理内联附件
            if (attachment.getInline() && StringUtils.hasText(attachment.getContentId())) {
                if (StringUtils.hasText(attachment.getFilePath())) {
                    helper.addInline(attachment.getContentId(), new File(attachment.getFilePath()));
                } else if (attachment.getContent() != null) {
                    helper.addInline(attachment.getContentId(), new ByteArrayResource(attachment.getContent()));
                }
            }
        }
    }

    /**
     * 生成邮件ID
     */
    private String generateMailId() {
        return "MAIL_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}