/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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
package com.agapsys.rcf.integration;

import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse;
import com.agapsys.jee.StacktraceErrorHandler;
import com.agapsys.rcf.RcfContainer;
import com.agapsys.rcf.integration.controllers.DtoController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DtoControllerTest {

    private RcfContainer rc;

    @Before
    public void before() {
        // Register controllers directly...
        rc = new RcfContainer<>()
            .registerController(DtoController.class)
            .setErrorHandler(new StacktraceErrorHandler());

        rc.start();
    }

    @After
    public void after() {
        rc.stop();
    }

    @Test
    public void testGetObject() {
        HttpResponse.StringResponse resp = rc.doRequest(new HttpGet("/dto/getObject"));
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals(String.format("{\"dtoVal\":%s}", 2), resp.getContentString());
    }

    @Test
    public void testGetList() {
        HttpResponse.StringResponse resp = rc.doRequest(new HttpGet("/dto/getList"));
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals(String.format("[{\"dtoVal\":%s},{\"dtoVal\":%s},{\"dtoVal\":%s}]", 0, 2, 4), resp.getContentString());
    }

    @Test
    public void testGetSet() {
        HttpResponse.StringResponse resp = rc.doRequest(new HttpGet("/dto/getSet"));
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals(String.format("[{\"dtoVal\":%s},{\"dtoVal\":%s},{\"dtoVal\":%s}]", 6, 8, 10), resp.getContentString());
    }

    @Test
    public void testGetMap() {
        HttpResponse.StringResponse resp = rc.doRequest(new HttpGet("/dto/getMap"));
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals(String.format("{\"a\":{\"dtoVal\":%s},\"b\":{\"dtoVal\":%s},\"c\":{\"dtoVal\":%s}}", 2, 6, 10), resp.getContentString());
    }

}
