package com.wool.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class WoolInfoDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题最长128字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String category;

    private String sourceUrl;

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
