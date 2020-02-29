package com.example.book.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 09:14
 * @description:
 */
@RestController
public class BookController {

    @RequestMapping("/available")
    public String available() {
        return "Spring in Action";
    }

    @RequestMapping("/checked-out")
    public String checkOut() {
        return "SpringBoot in Action";
    }
}
