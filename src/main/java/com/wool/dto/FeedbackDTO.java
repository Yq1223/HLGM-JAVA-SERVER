package com.wool.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class FeedbackDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题不能超过128字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
