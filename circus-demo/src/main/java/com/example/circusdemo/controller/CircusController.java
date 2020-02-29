package com.example.circusdemo.controller;

import com.example.circusdemo.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 17:18
 * @description:
 */
@RestController
public class CircusController {

    @Autowired
    private BookService bookService;

    @RequestMapping("/available")
    public String available() {
        return bookService.available();
    }
}
