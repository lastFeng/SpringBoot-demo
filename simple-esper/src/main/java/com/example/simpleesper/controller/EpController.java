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
package com.example.simpleesper.controller;

import com.espertech.esper.client.EPException;
import com.example.simpleesper.domain.PersonEvent;
import com.example.simpleesper.service.EpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p> Title: </p>
 *
 * <p> Description: </p>
 *
 * @author: Guo Weifeng
 * @version: 1.0
 * @create: 2020/3/2 14:05
 */
@RestController
public class EpController {

    @Autowired
    private EpService epService;

    @RequestMapping("/send")
    public String sendMessage() {
        String epl = "select * from person";
        PersonEvent event = new PersonEvent("Hello", 30);

        try {
            epService.sendEvent(epl, event);
            return "执行成功";
        } catch (EPException ex) {
            ex.printStackTrace();
            return "有异常发生";
        }
    }
}