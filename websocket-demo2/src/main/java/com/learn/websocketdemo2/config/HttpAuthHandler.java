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
package com.learn.websocketdemo2.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static com.learn.websocketdemo2.constant.Constant.TOKEN;

/**
 * <p> Title: </p>
 *
 * <p> Description: </p>
 *
 * @author: Guo Weifeng
 * @version: 1.0
 * @create: 2020/5/25 16:47
 */
@Component
public class HttpAuthHandler extends TextWebSocketHandler {
    private static ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * socket建立连接
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object token = session.getAttributes().get(TOKEN);
        if (token != null) {
            // 连接成功，放入在线缓存
            SESSION_POOL.put(token.toString(), session);
        } else {
            throw new RuntimeException("用户登录已失效");
        }
    }

    /**
     * 接收消息事件
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String playLoad = message.getPayload();
        Object token = session.getAttributes().get(TOKEN);
        System.out.println("服务器收到" + token + "消息：" + playLoad);
        session.sendMessage(new TextMessage("服务器发送给" + token + "消息：" + playLoad + " "
        + LocalDateTime.now().toString()));
    }

    /**
     * 断开连接
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object token = session.getAttributes().get(TOKEN);
        if (token != null) {
            SESSION_POOL.remove(token);
        }
    }
}