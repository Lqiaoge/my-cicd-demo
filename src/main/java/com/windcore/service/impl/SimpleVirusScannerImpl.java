package com.windcore.service.impl;

import com.windcore.dto.VirusScanResult;
import com.windcore.service.VirusScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 简单的病毒扫描器实现
 * 这是一个示例实现，实际生产环境中应该集成真正的病毒扫描引擎如ClamAV
 */
@Service
@Slf4j
public class SimpleVirusScannerImpl implements VirusScanner {

    // 简单的恶意文件特征码（示例）
    private static final List<String> MALICIOUS_SIGNATURES = Arrays.asList(
            "EICAR-STANDARD-ANTIVIRUS-TEST-FILE",
            "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR",
            "malware",
            "virus"
    );

    @Override
    public VirusScanResult scan(byte[] fileContent) {
        log.info("开始病毒扫描，文件大小: {} bytes", fileContent.length);
        
        try {
            // 将文件内容转换为字符串进行简单检查
            String content = new String(fileContent).toLowerCase();
            
            // 检查是否包含恶意特征码
            for (String signature : MALICIOUS_SIGNATURES) {
                if (content.contains(signature.toLowerCase())) {
                    log.warn("检测到恶意特征码: {}", signature);
                    return VirusScanResult.infected(Arrays.asList(signature));
                }
            }
            
            // 检查文件大小是否异常（简单启发式检查）
            if (fileContent.length == 0) {
                log.warn("检测到空文件，可能存在风险");
                return VirusScanResult.infected(Arrays.asList("Empty file detected"));
            }
            
            log.info("病毒扫描完成，文件安全");
            return VirusScanResult.clean();
            
        } catch (Exception e) {
            log.error("病毒扫描过程中发生错误", e);
            // 出现错误时，为了安全起见，认为文件有问题
            return VirusScanResult.infected(Arrays.asList("Scan error: " + e.getMessage()));
        }
    }
}