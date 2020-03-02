/*
 * Copyright 2001-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.websocketdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * <p> Title: </p>
 *
 * <p> Description: </p>
 *
 * @author: Guo Weifeng
 * @version: 1.0
 * @create: 2020/3/2 14:52
 */
@Configuration
// @EnableWebSocketMessageBroker注解用于开启使用STOMP协议来传输基于代理（MessageBroker）的消息，这时候控制器（controller）
// 开始支持@MessageMapping,就像是使用@requestMapping一样
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //注册一个 Stomp 的节点(endpoint),并指定使用 SockJS 协议。
        registry.addEndpoint("/endpointNasus").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 广播式配置名为 /nasus 消息代理 , 这个消息代理必须和 controller 中的 @SendTo 配置的地址前缀一样或者全匹配
        registry.enableSimpleBroker("/nasus");
    }
}