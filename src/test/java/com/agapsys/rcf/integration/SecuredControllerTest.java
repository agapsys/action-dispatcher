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

import com.agapsys.http.HttpClient;
import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.rcf.ServletContainerBuilder;
import com.agapsys.rcf.integration.controllers.SecuredController;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.sevlet.container.StacktraceErrorHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SecuredControllerTest {

    private ServletContainer sc;
    private StringResponse resp;

    @Before
    public void before() {
        sc = new ServletContainerBuilder().registerController(SecuredController.class).setErrorHandler(new StacktraceErrorHandler()).build();
        sc.startServer();
    }

    @After
    public void after() {
        sc.stopServer();
    }

    @Test
    public void testUnlogged() {
        resp = sc.doRequest(new HttpGet("/secured/securedGet"));
        Assert.assertEquals(401, resp.getStatusCode());
        resp = sc.doRequest(new HttpGet("/secured/securedGetWithRoles"));
        Assert.assertEquals(401, resp.getStatusCode());
    }

    @Test
    public void testLoggedWithoutRoles() {
        HttpClient client = new HttpClient();

        resp = sc.doRequest(client, new HttpGet("/secured/logUser?%s=%s", SecuredController.PARAM_ROLE, ""));

        resp = sc.doRequest(client, new HttpGet("/secured/logUser"));
        Assert.assertEquals(200, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGet"));
        Assert.assertEquals(200, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGetWithRoles"));
        Assert.assertEquals(403, resp.getStatusCode());
    }

    @Test
    public void testLoggedWithRoles() {
        HttpClient client = new HttpClient();

        resp = sc.doRequest(client, new HttpGet("/secured/logUser?%s=%s", SecuredController.PARAM_ROLE, SecuredController.ROLE));
        Assert.assertEquals(200, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGet"));
        Assert.assertEquals(200, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGetWithRoles"));
        Assert.assertEquals(200, resp.getStatusCode());
    }

    @Test
    public void testSignInAndSignOut() {
        HttpClient client = new HttpClient();

        // Log (with roles)
        resp = sc.doRequest(client, new HttpGet("/secured/logUser?%s=%s", SecuredController.PARAM_ROLE, SecuredController.ROLE));
        Assert.assertEquals(200, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGet"));
        Assert.assertEquals(200, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGetWithRoles"));
        Assert.assertEquals(200, resp.getStatusCode());

        // Unlogging
        resp = sc.doRequest(client, new HttpGet("/secured/unlogUser"));
        Assert.assertEquals(200, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGet"));
        Assert.assertEquals(401, resp.getStatusCode());
        resp = sc.doRequest(client, new HttpGet("/secured/securedGetWithRoles"));
        Assert.assertEquals(401, resp.getStatusCode());
    }
    // =========================================================================
}
