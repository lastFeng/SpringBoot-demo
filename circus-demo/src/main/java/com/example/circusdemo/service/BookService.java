package com.example.circusdemo.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 17:21
 * @description:
 */
@Service
public class BookService {

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "unavailable")
    public String available() {
        URI uri = URI.create("http://localhost:8090/available");

        return this.restTemplate.getForObject(uri, String.class);
    }

    public String unavailable() {
        return "fallbackMethod active";
    }
}
