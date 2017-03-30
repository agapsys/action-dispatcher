/*
 * Copyright 2017 Agapsys Tecnologia Ltda-ME.
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

import com.agapsys.http.HttpResponse;
import com.agapsys.http.StringEntityRequest;
import com.agapsys.jee.StacktraceErrorHandler;
import com.agapsys.rcf.RcfContainer;
import com.agapsys.rcf.integration.controllers.JsonController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonControllerTest {
    private RcfContainer rc;
    private HttpResponse.StringResponse resp;

    @Before
    public void before() {
        rc = RcfContainer.newInstance()
            .registerController(JsonController.class)
            .setErrorHandler(new StacktraceErrorHandler());

        rc.start();
    }

    @After
    public void after() {
        rc.stop();
    }
    
    @Test
    public void testList() {
        StringEntityRequest.StringEntityPost post = new StringEntityRequest.StringEntityPost("application/json", "utf-8", "/json/list");
        post.setContentBody("[\"zero\", \"one\"]");
        
        resp = rc.doRequest(post);
        
        if (resp.getStatusCode() == 500)
            System.out.println(resp.getContentString());
        
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals("\"OK\"", resp.getContentString());
    }
    
    @Test
    public void testInvalidListAction1() {
        StringEntityRequest.StringEntityPost post = new StringEntityRequest.StringEntityPost("application/json", "utf-8", "/json/wildcardList");
        post.setContentBody("[\"zero\", \"one\"]");
        
        resp = rc.doRequest(post);
        
        Assert.assertEquals(500, resp.getStatusCode());
        Assert.assertTrue(resp.getContentString().contains("Unsupported list element type"));
    }
    
    @Test
    public void testInvalidListAction2() {
        StringEntityRequest.StringEntityPost post = new StringEntityRequest.StringEntityPost("application/json", "utf-8", "/json/genericList");
        post.setContentBody("[\"zero\", \"one\"]");
        
        resp = rc.doRequest(post);
        
        Assert.assertEquals(500, resp.getStatusCode());
        Assert.assertTrue(resp.getContentString().contains("Missing list element type"));
    }
    
    @Test
    public void testListAndArgs() {
        StringEntityRequest.StringEntityPost post = new StringEntityRequest.StringEntityPost("application/json", "utf-8", "/json/listAndArgs");
        post.setContentBody("[\"zero\", \"one\"]");
        
        resp = rc.doRequest(post);
        
        if (resp.getStatusCode() == 500)
            System.out.println(resp.getContentString());
        
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals("\"OK\"", resp.getContentString());
    }
    
    @Test
    public void testDto() {
        StringEntityRequest.StringEntityPost post = new StringEntityRequest.StringEntityPost("application/json", "utf-8", "/json/dto");
        post.setContentBody("{\"string\": \"string\", \"integer\": 12}");
        
        resp = rc.doRequest(post);
        
        if (resp.getStatusCode() != 200)
            System.out.println(resp.getContentString());
        
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals("\"OK\"", resp.getContentString());
    }
    
    @Test
    public void testDtoAndArgs() {
        StringEntityRequest.StringEntityPost post = new StringEntityRequest.StringEntityPost("application/json", "utf-8", "/json/dtoAndArgs");
        post.setContentBody("{\"string\": \"string\", \"integer\": 12}");
        
        resp = rc.doRequest(post);
        
        if (resp.getStatusCode() != 200)
            System.out.println(resp.getContentString());
        
        Assert.assertEquals(200, resp.getStatusCode());
        Assert.assertEquals("\"OK\"", resp.getContentString());
    }
    
    @Test
    public void sendInvalidDto() {
        StringEntityRequest.StringEntityPost post = new StringEntityRequest.StringEntityPost("application/json", "utf-8", "/json/dto");
        post.setContentBody("12");
        
        resp = rc.doRequest(post);
        String strResp = resp.getContentString();
        
        if (resp.getStatusCode() != 200)
            System.out.println(strResp);
        
        Assert.assertEquals(400, resp.getStatusCode());
    }
}
