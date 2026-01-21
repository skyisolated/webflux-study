package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.*;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 看看webflux相比于mvc，注解方面的变化
 */
@RestController
public class TestController {
    /**
     * 参数和以前基本差不多，除了ServerWebExchange
     * @param session
     * @param exchange
     * @param method
     * @return
     */
    @GetMapping("/test")
    public String test(WebSession session,
                       ServerWebExchange exchange,
                       HttpMethod method){
        JSONObject info = new JSONObject();
        String sessionId = session.getId();
        info.put("sessionId", sessionId);
        Map<String, Object> attributes = session.getAttributes();
        info.put("attributes", attributes);

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        RequestPath path = request.getPath();
        MultiValueMap<String, ResponseCookie> cookies = response.getCookies();
        HttpHeaders headers = request.getHeaders();

        Map<String, Object> header = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            header.put(entry.getKey(), entry.getValue());
        }
        info.put("headers", header);

        info.put("path", path.value());
        info.put("cookies", JSON.toJSONString(cookies));

        String name = method.name();
        info.put("method", name);
        return info.toJSONString();

    }

    @GetMapping("/test2")
    public String test2(@RequestHeader MultiValueMap<String, String> header,
                        ServerWebExchange exchange){
        JSONObject info = new JSONObject();
        String connection = header.getFirst("Connection");
        info.put("Connection", connection);
        ServerHttpResponse response = exchange.getResponse();
        ResponseCookie build = ResponseCookie.from("theme", "dark").build();
        response.addCookie(build);
        return info.toJSONString();
    }

    /**
     * ResponseEntity可以让你自由的定义的响应体
     * @return
     */
    @GetMapping("/entity")
    public ResponseEntity<String> entity(){
        return ResponseEntity.status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .header("name", "zhangsan")
                .body("hello world");
    }
}
