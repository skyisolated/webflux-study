package com.example.config;

import com.example.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.*;

@Configuration
public class WebFunctionConfig {
    // 函数式web，感觉有点返祖，远不如传统controller写起来方便，了解下就行
    @Bean
    public RouterFunction<ServerResponse> userRoute(UserHandler userHandler){
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route()
                .GET("/users/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON), userHandler::getUserById)
                .GET("/users", RequestPredicates
                                .accept(MediaType.APPLICATION_JSON),
                        userHandler::listUsers)
                .POST("/users", RequestPredicates.accept(MediaType.APPLICATION_JSON), userHandler::addUser)
                .PUT("/users", RequestPredicates.accept(MediaType.APPLICATION_JSON), userHandler::updateUser)
                .DELETE("/users/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON), userHandler::deleteUser)
                .build();
        return routerFunction;
    }
}
