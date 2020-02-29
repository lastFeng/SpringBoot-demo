package com.example.soapdemo.service;

import com.example.soapdemo.domain.User;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 15:07
 * @description:
 * 接口部分的WebService下的属性需要在实现类中书写
 */
@WebService
public interface UserService {

    /**
     * 标注该方法为WebService暴露方法
     * 用于向外公布
     * 它修饰的方法是webservice方法，去掉也没影响的，类似一个注释信息。
     * @param userId
     * @return
     */
    @WebMethod
    public User getUser(@WebParam(name = "userId") String userId);

    @WebMethod
    @WebResult(name = "String", targetNamespace = "")
    public String getUserName(@WebParam(name = "userId") String userId);
}
