package com.wool.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WxSubscribeUtil {

    private static final Logger log = LoggerFactory.getLogger(WxSubscribeUtil.class);

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 access_token
     */
    private String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            Map<String, Object> map = objectMapper.readValue(resp.getBody(), Map.class);
            return (String) map.get("access_token");
        } catch (Exception e) {
            log.error("获取access_token失败", e);
            return null;
        }
    }

    /**
     * 发送订阅消息
     * @param openIds  接收者的 openid 列表
     * @param tplId    模板 ID
     * @param page     点击消息跳转的小程序页面
     * @param data     模板数据
     */
    public void sendSubscribeMessage(List<String> openIds, String tplId, String page, Map<String, Object> data) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            log.error("无法获取access_token，跳过发送订阅消息");
            return;
        }

        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

        for (String openId : openIds) {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("touser", openId);
                body.put("template_id", tplId);
                body.put("page", page);
                body.put("data", data);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

                ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
                log.info("订阅消息发送结果: openId={}, resp={}", openId, resp.getBody());
            } catch (Exception e) {
                log.error("订阅消息发送失败: openId={}", openId, e);
            }
        }
    }
}