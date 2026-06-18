package com.wool.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

/**
 * 薅羊毛信息批量导入 Excel 行映射
 */
public class WoolInfoImportDTO {

    @ExcelProperty("标题")
    @ColumnWidth(30)
    private String title;

    @ExcelProperty("内容")
    @ColumnWidth(50)
    private String content;

    @ExcelProperty("分类")
    @ColumnWidth(15)
    private String category;

    @ExcelProperty("来源链接")
    @ColumnWidth(40)
    private String sourceUrl;

    @ExcelProperty("领取步骤")
    @ColumnWidth(50)
    private String claimSteps;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getClaimSteps() { return claimSteps; }
    public void setClaimSteps(String claimSteps) { this.claimSteps = claimSteps; }
}
