package com.example.soapdemo.service.impl;

import com.example.soapdemo.domain.User;
import com.example.soapdemo.service.UserService;
import org.springframework.stereotype.Component;

import javax.jws.WebService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 15:10
 * @description:
 *
 *
 *  * ServiceName: 对外发布的服务名
 *  * targetNamespace: 指定你想要的名称空间，通常使用包名反转
 *
 */

@Component
@WebService
public class UserServiceImpl implements UserService {

    public static final Map<String, User> userMap = new ConcurrentHashMap<>();

    static  {
        System.out.println("向实体类插入数据");
        User user = new User();
        user.setId(111);
        user.setUserName("test1");
        userMap.put(String.valueOf(user.getId()), user);

        user = new User();
        user.setId(112);
        user.setUserName("test2");
        userMap.put(String.valueOf(user.getId()), user);

        user = new User();
        user.setId(113);
        user.setUserName("test3");
        userMap.put(String.valueOf(user.getId()), user);
        System.out.println("实体类插入数据完毕");
    }

    @Override
    public User getUser(String userId) {
        System.out.println("userMap是：" + userMap);
        return userMap.get(userId);
    }

    @Override
    public String getUserName(String userId) {
        return "userId为：" + userMap.get(userId).getUserName();
    }
}

