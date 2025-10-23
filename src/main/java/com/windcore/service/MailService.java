package com.windcore.service;

import com.windcore.model.MailMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 邮件服务接口
 * 
 * @author windcore
 */
public interface MailService {

    /**
     * 发送简单文本邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 邮件ID
     */
    String sendSimpleMail(String to, String subject, String content);

    /**
     * 发送简单文本邮件（多收件人）
     * 
     * @param toList 收件人列表
     * @param subject 主题
     * @param content 内容
     * @return 邮件ID
     */
    String sendSimpleMail(List<String> toList, String subject, String content);

    /**
     * 发送HTML邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 邮件ID
     */
    String sendHtmlMail(String to, String subject, String htmlContent);

    /**
     * 发送HTML邮件（多收件人）
     * 
     * @param toList 收件人列表
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 邮件ID
     */
    String sendHtmlMail(List<String> toList, String subject, String htmlContent);

    /**
     * 发送模板邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param templateName 模板名称
     * @param variables 模板变量
     * @return 邮件ID
     */
    String sendTemplateMail(String to, String subject, String templateName, Map<String, Object> variables);

    /**
     * 发送模板邮件（多收件人）
     * 
     * @param toList 收件人列表
     * @param subject 主题
     * @param templateName 模板名称
     * @param variables 模板变量
     * @return 邮件ID
     */
    String sendTemplateMail(List<String> toList, String subject, String templateName, Map<String, Object> variables);

    /**
     * 发送邮件（完整参数）
     * 
     * @param mailMessage 邮件消息对象
     * @return 邮件ID
     */
    String sendMail(MailMessage mailMessage);

    /**
     * 异步发送邮件
     * 
     * @param mailMessage 邮件消息对象
     * @return CompletableFuture<String> 异步结果
     */
    CompletableFuture<String> sendMailAsync(MailMessage mailMessage);

    /**
     * 批量发送邮件
     * 
     * @param mailMessages 邮件消息列表
     * @return 发送结果列表
     */
    List<String> sendBatchMail(List<MailMessage> mailMessages);

    /**
     * 异步批量发送邮件
     * 
     * @param mailMessages 邮件消息列表
     * @return CompletableFuture<List<String>> 异步结果
     */
    CompletableFuture<List<String>> sendBatchMailAsync(List<MailMessage> mailMessages);

    /**
     * 获取邮件发送状态
     * 
     * @param mailId 邮件ID
     * @return 邮件消息对象
     */
    MailMessage getMailStatus(String mailId);

    /**
     * 重新发送失败的邮件
     * 
     * @param mailId 邮件ID
     * @return 是否成功
     */
    boolean resendMail(String mailId);

    /**
     * 取消待发送的邮件
     * 
     * @param mailId 邮件ID
     * @return 是否成功
     */
    boolean cancelMail(String mailId);

    /**
     * 获取邮件发送历史
     * 
     * @param page 页码
     * @param size 页大小
     * @return 邮件列表
     */
    List<MailMessage> getMailHistory(int page, int size);

    /**
     * 测试邮件服务连接
     * 
     * @return 是否连接成功
     */
    boolean testConnection();
}