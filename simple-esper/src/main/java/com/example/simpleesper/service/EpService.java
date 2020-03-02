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
package com.example.simpleesper.service;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.example.simpleesper.domain.PersonEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p> Title: </p>
 *
 * <p> Description: </p>
 *
 * @author: Guo Weifeng
 * @version: 1.0
 * @create: 2020/3/2 13:45
 */

@Service
public class EpService {
    @Autowired
    private EPServiceProvider epServiceProvider;

    @Autowired
    private EPAdministrator epAdministrator;

    public void sendEvent(String epl, PersonEvent message) throws EPException {
        EPStatement statement = epAdministrator.createEPL(epl);
        statement.addListener((newData, oldData) -> {
            String name = (String) newData[0].get("name");
            int age = (int) newData[0].get("age");
            System.out.println(String.format("Name: %s, Age: %d", name, age));
        });
        epServiceProvider.getEPRuntime().sendEvent(message);
    }
}