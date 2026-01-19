package com.example.handler;

import com.alibaba.fastjson2.JSONObject;
import com.example.model.User;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Service
public class UserHandler {
    public ServerResponse getUserById(ServerRequest request){
        String id = request.pathVariable("id");
        log.info("id={}", id);
        User user = new User(Long.valueOf(id), "张三", 18,"男");
        return ServerResponse.ok().body(user);
    }
    public ServerResponse listUsers(ServerRequest request){
        List<User> list = List.of(new User(1L, "张三", 18, "男"),
                new User(2L, "李四", 22, "女"),
                new User(3L, "王五", 20, "男"));
        String page = request.param("page").get();
        String limit = request.param("limit").get();
        String name = request.param("name").get();
        log.info("page={}, limit={}, name={}", page, limit, name);
        JSONObject info = new JSONObject();
        info.put("data", list);
        info.put("total", list.size());
        info.put("page", page);
        info.put("pageSize", limit);
        return ServerResponse.ok().body(info);
    }
    public ServerResponse addUser(ServerRequest request) throws ServletException, IOException {
        User user = request.body(User.class);
        log.info("user={}", user);
        return ServerResponse.ok().body("添加成功");
    }
    public ServerResponse updateUser(ServerRequest request) throws ServletException, IOException {
        User user = request.body(User.class);
        log.info("update, user={}", user);
        return ServerResponse.ok().body("更新成功");
    }
    public ServerResponse deleteUser(ServerRequest request){
        String id = request.pathVariable("id");
        log.info("delete, id={}", id);
        return ServerResponse.ok().body("删除成功");
    }
}
