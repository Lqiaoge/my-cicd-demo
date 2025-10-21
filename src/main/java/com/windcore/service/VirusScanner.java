package com.windcore.service;

import com.windcore.dto.VirusScanResult;

/**
 * 病毒扫描服务接口
 */
public interface VirusScanner {
    
    /**
     * 扫描文件内容
     * @param fileContent 文件内容字节数组
     * @return 扫描结果
     */
    VirusScanResult scan(byte[] fileContent);
}