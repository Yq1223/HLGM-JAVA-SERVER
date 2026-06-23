package com.wool.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Component
public class WxSubscribeUtil {

    private static final Logger log = LoggerFactory.getLogger(WxSubscribeUtil.class);

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WxSubscribeUtil() {
        RestTemplate rt;
        try {
            // 信任所有证书（解决容器环境 SSL 证书链不完整的问题）
            TrustStrategy trustAll = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, trustAll)
                    .build();

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    sslContext, NoopHostnameVerifier.INSTANCE);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(10000);
            factory.setConnectionRequestTimeout(10000);

            rt = new RestTemplate(factory);
            log.info("[WxSubscribeUtil] RestTemplate 初始化成功（已信任所有SSL证书）");
        } catch (Exception e) {
            log.error("[WxSubscribeUtil] SSL初始化失败，使用默认RestTemplate", e);
            rt = new RestTemplate();
        }
        this.restTemplate = rt;
    }

    /**
     * 获取 access_token
     */
    private String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
        try {
            log.info("[access_token] 请求微信接口, appid={}", appId);
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            log.info("[access_token] 微信响应: status={}, body={}", resp.getStatusCode(), resp.getBody());
            Map<String, Object> map = objectMapper.readValue(resp.getBody(), Map.class);
            if (map.containsKey("access_token")) {
                log.info("[access_token] 获取成功");
                return (String) map.get("access_token");
            } else {
                log.error("[access_token] 获取失败: errcode={}, errmsg={}", map.get("errcode"), map.get("errmsg"));
                return null;
            }
        } catch (Exception e) {
            log.error("[access_token] 获取异常: type={}, message={}", e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 发送订阅消息（单个用户）
     * @param openId  接收者的 openid
     * @param tplId   模板 ID
     * @param page    点击消息跳转的小程序页面
     * @param data    模板数据
     * @return true=发送成功, false=发送失败
     */
    public boolean sendSubscribeMessage(String openId, String tplId, String page, Map<String, Object> data) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            log.error("[订阅消息] 无法获取access_token，跳过发送");
            return false;
        }

        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

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
            log.info("[订阅消息] 微信API响应: openId={}, status={}, body={}", openId, resp.getStatusCode(), resp.getBody());

            // 解析微信返回的错误码
            Map<String, Object> result = objectMapper.readValue(resp.getBody(), Map.class);
            Integer errcode = (Integer) result.get("errcode");
            if (errcode != null && errcode != 0) {
                log.error("[订阅消息] 微信返回错误: errcode={}, errmsg={}, openId={}", errcode, result.get("errmsg"), openId);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[订阅消息] 发送异常: openId={}", openId, e);
            return false;
        }
    }
}
