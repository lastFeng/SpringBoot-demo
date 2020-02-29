package com.example.soapdemo.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 15:06
 * @description:
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = -1242343L;

    private int id;
    private String userName;
    private String passWord;
    private String userSex;
    private String nickName;
}
