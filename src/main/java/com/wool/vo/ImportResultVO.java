package com.wool.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入结果
 */
public class ImportResultVO {

    /** 成功导入条数 */
    private int successCount;

    /** 失败条数 */
    private int failCount;

    /** 失败详情 */
    private List<String> failDetails = new ArrayList<>();

    public void addSuccess() {
        this.successCount++;
    }

    public void addFail(String reason) {
        this.failCount++;
        this.failDetails.add(reason);
    }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }

    public List<String> getFailDetails() { return failDetails; }
    public void setFailDetails(List<String> failDetails) { this.failDetails = failDetails; }
}
