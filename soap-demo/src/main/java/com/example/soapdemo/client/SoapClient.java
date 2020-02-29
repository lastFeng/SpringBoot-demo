package com.example.soapdemo.client;

import com.example.soapdemo.domain.User;
import com.example.soapdemo.service.UserService;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.http.client.methods.HttpPost;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 15:35
 * @description:
 */
public class SoapClient {
    public static void main(String[] args) {
        proxyFactorySoapClient();
    }

    /**
     * 代理类工厂方式，需要拿到对方的接口地址
     */
    public static void proxyFactorySoapClient() {
        // 接口地址
        String address = "http://127.0.0.1:8080/services/user?wsdl";
        // 代理工厂
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();

        try{
            // 设置代理地址
            jaxWsProxyFactoryBean.setAddress(address);
            // 设置接口类型
            jaxWsProxyFactoryBean.setServiceClass(UserService.class);
            // 创建一个代理接口实现
            UserService us = (UserService) jaxWsProxyFactoryBean.create();
            // 准备数据
            String userId = "111";
            // 调用代理接口的方法并返回结果
            String result = us.getUserName(userId);
            System.out.println("返回结果：" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
