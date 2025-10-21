package com.windcore.dto;

import java.util.List;

/**
 * 病毒扫描结果DTO
 */
public class VirusScanResult {
    private boolean clean;
    private List<String> threats;

    public VirusScanResult(boolean clean, List<String> threats) {
        this.clean = clean;
        this.threats = threats;
    }

    /**
     * 创建干净的扫描结果
     * @return 干净的扫描结果
     */
    public static VirusScanResult clean() {
        return new VirusScanResult(true, null);
    }

    /**
     * 创建感染的扫描结果
     * @param threats 威胁列表
     * @return 感染的扫描结果
     */
    public static VirusScanResult infected(List<String> threats) {
        return new VirusScanResult(false, threats);
    }

    public boolean isClean() {
        return clean;
    }

    public List<String> getThreats() {
        return threats;
    }
}