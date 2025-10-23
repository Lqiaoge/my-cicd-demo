package com.windcore.service;

import com.windcore.model.MailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 邮件队列服务
 * 
 * @author windcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailQueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MailService mailService;

    private static final String MAIL_QUEUE_KEY = "mail:queue";
    private static final String MAIL_RETRY_QUEUE_KEY = "mail:retry:queue";
    private static final String MAIL_FAILED_QUEUE_KEY = "mail:failed:queue";
    private static final String MAIL_PROCESSING_KEY = "mail:processing";

    /**
     * 添加邮件到队列
     * 
     * @param mailMessage 邮件消息
     */
    public void addToQueue(MailMessage mailMessage) {
        try {
            // 设置邮件状态为待发送
            mailMessage.setStatus(MailMessage.MailStatus.PENDING);
            mailMessage.setCreateTime(LocalDateTime.now());
            
            // 根据优先级添加到不同的队列
            String queueKey = getQueueKeyByPriority(mailMessage.getPriority());
            
            // 添加到Redis队列
            redisTemplate.opsForList().leftPush(queueKey, mailMessage);
            
            // 设置邮件信息的过期时间（7天）
            String mailKey = "mail:info:" + mailMessage.getId();
            redisTemplate.opsForValue().set(mailKey, mailMessage, 7, TimeUnit.DAYS);
            
            log.info("邮件已添加到队列，邮件ID: {}, 优先级: {}", mailMessage.getId(), mailMessage.getPriority());
            
        } catch (Exception e) {
            log.error("添加邮件到队列失败，邮件ID: {}", mailMessage.getId(), e);
            throw new RuntimeException("添加邮件到队列失败", e);
        }
    }

    /**
     * 从队列中获取邮件并发送
     */
    @Async("mailTaskExecutor")
    @Scheduled(fixedDelay = 1000) // 每秒检查一次
    public void processMailQueue() {
        try {
            // 按优先级处理队列（优先级1最高）
            for (int priority = 1; priority <= 5; priority++) {
                String queueKey = getQueueKeyByPriority(priority);
                MailMessage mailMessage = (MailMessage) redisTemplate.opsForList().rightPop(queueKey);
                
                if (mailMessage != null) {
                    processMailMessage(mailMessage);
                    break; // 处理一封邮件后退出，避免阻塞
                }
            }
            
        } catch (Exception e) {
            log.error("处理邮件队列时发生错误", e);
        }
    }

    /**
     * 处理重试队列
     */
    @Scheduled(fixedDelay = 30000) // 每30秒检查一次重试队列
    public void processRetryQueue() {
        try {
            MailMessage mailMessage = (MailMessage) redisTemplate.opsForList().rightPop(MAIL_RETRY_QUEUE_KEY);
            
            if (mailMessage != null) {
                // 检查是否达到最大重试次数
                if (mailMessage.getRetryCount() < mailMessage.getMaxRetryCount()) {
                    log.info("重试发送邮件，邮件ID: {}, 重试次数: {}", 
                            mailMessage.getId(), mailMessage.getRetryCount());
                    processMailMessage(mailMessage);
                } else {
                    // 达到最大重试次数，移到失败队列
                    mailMessage.setStatus(MailMessage.MailStatus.FAILED);
                    redisTemplate.opsForList().leftPush(MAIL_FAILED_QUEUE_KEY, mailMessage);
                    log.warn("邮件发送失败，已达到最大重试次数，邮件ID: {}", mailMessage.getId());
                }
            }
            
        } catch (Exception e) {
            log.error("处理重试队列时发生错误", e);
        }
    }

    /**
     * 处理单个邮件消息
     */
    private void processMailMessage(MailMessage mailMessage) {
        try {
            // 添加到处理中集合
            redisTemplate.opsForSet().add(MAIL_PROCESSING_KEY, mailMessage.getId());
            
            // 发送邮件
            mailService.sendMail(mailMessage);
            
            // 发送成功，从处理中集合移除
            redisTemplate.opsForSet().remove(MAIL_PROCESSING_KEY, mailMessage.getId());
            
            log.info("邮件发送成功，邮件ID: {}", mailMessage.getId());
            
        } catch (Exception e) {
            log.error("邮件发送失败，邮件ID: {}", mailMessage.getId(), e);
            
            // 从处理中集合移除
            redisTemplate.opsForSet().remove(MAIL_PROCESSING_KEY, mailMessage.getId());
            
            // 增加重试次数
            mailMessage.setRetryCount(mailMessage.getRetryCount() + 1);
            mailMessage.setErrorMessage(e.getMessage());
            
            // 添加到重试队列
            if (mailMessage.getRetryCount() < mailMessage.getMaxRetryCount()) {
                mailMessage.setStatus(MailMessage.MailStatus.RETRY);
                redisTemplate.opsForList().leftPush(MAIL_RETRY_QUEUE_KEY, mailMessage);
            } else {
                mailMessage.setStatus(MailMessage.MailStatus.FAILED);
                redisTemplate.opsForList().leftPush(MAIL_FAILED_QUEUE_KEY, mailMessage);
            }
            
            // 更新邮件信息
            String mailKey = "mail:info:" + mailMessage.getId();
            redisTemplate.opsForValue().set(mailKey, mailMessage, 7, TimeUnit.DAYS);
        }
    }

    /**
     * 根据优先级获取队列键
     */
    private String getQueueKeyByPriority(Integer priority) {
        if (priority == null) {
            priority = 3; // 默认优先级
        }
        return MAIL_QUEUE_KEY + ":" + priority;
    }

    /**
     * 获取队列状态
     */
    public QueueStatus getQueueStatus() {
        QueueStatus status = new QueueStatus();
        
        // 统计各优先级队列长度
        for (int priority = 1; priority <= 5; priority++) {
            String queueKey = getQueueKeyByPriority(priority);
            Long size = redisTemplate.opsForList().size(queueKey);
            status.addPriorityQueueSize(priority, size != null ? size.intValue() : 0);
        }
        
        // 统计重试队列长度
        Long retrySize = redisTemplate.opsForList().size(MAIL_RETRY_QUEUE_KEY);
        status.setRetryQueueSize(retrySize != null ? retrySize.intValue() : 0);
        
        // 统计失败队列长度
        Long failedSize = redisTemplate.opsForList().size(MAIL_FAILED_QUEUE_KEY);
        status.setFailedQueueSize(failedSize != null ? failedSize.intValue() : 0);
        
        // 统计处理中的邮件数量
        Long processingSize = redisTemplate.opsForSet().size(MAIL_PROCESSING_KEY);
        status.setProcessingCount(processingSize != null ? processingSize.intValue() : 0);
        
        return status;
    }

    /**
     * 清理失败的邮件（可定期执行）
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupFailedMails() {
        try {
            // 清理7天前的失败邮件
            // 这里可以根据实际需求实现清理逻辑
            log.info("开始清理失败的邮件...");
            
            // 示例：清理失败队列中的过期邮件
            // 实际实现时可以根据邮件的创建时间进行判断
            
        } catch (Exception e) {
            log.error("清理失败邮件时发生错误", e);
        }
    }

    /**
     * 队列状态类
     */
    public static class QueueStatus {
        private final int[] priorityQueueSizes = new int[5]; // 优先级1-5的队列大小
        private int retryQueueSize;
        private int failedQueueSize;
        private int processingCount;

        public void addPriorityQueueSize(int priority, int size) {
            if (priority >= 1 && priority <= 5) {
                priorityQueueSizes[priority - 1] = size;
            }
        }

        public int getPriorityQueueSize(int priority) {
            if (priority >= 1 && priority <= 5) {
                return priorityQueueSizes[priority - 1];
            }
            return 0;
        }

        public int getTotalQueueSize() {
            int total = 0;
            for (int size : priorityQueueSizes) {
                total += size;
            }
            return total + retryQueueSize;
        }

        // Getters and Setters
        public int[] getPriorityQueueSizes() { return priorityQueueSizes; }
        public int getRetryQueueSize() { return retryQueueSize; }
        public void setRetryQueueSize(int retryQueueSize) { this.retryQueueSize = retryQueueSize; }
        public int getFailedQueueSize() { return failedQueueSize; }
        public void setFailedQueueSize(int failedQueueSize) { this.failedQueueSize = failedQueueSize; }
        public int getProcessingCount() { return processingCount; }
        public void setProcessingCount(int processingCount) { this.processingCount = processingCount; }
    }
}