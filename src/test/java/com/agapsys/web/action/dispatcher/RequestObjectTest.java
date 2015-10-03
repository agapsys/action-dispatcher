/*
 * Copyright 2015 Agapsys Tecnologia Ltda-ME.
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

package com.agapsys.web.action.dispatcher;

import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.HttpPost;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.web.action.dispatcher.servlets.TestObjectRequestServlet;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RequestObjectTest {
	private ServletContainer sc;
	
	@Before
	public void before() {
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerServlet(TestObjectRequestServlet.class);
		
		sc.registerContext(context);
		sc.startServer();
	}
	
	@After
	public void after() {
		sc.stopServer();
	}
	
	@Test
	public void sendValidObject() {
		String validJson = "{\"key\":1,\"value\":\"value\"}";
		HttpPost post = new HttpPost(sc, "/post");
		post.setContentType(ContentType.APPLICATION_JSON);
		post.setContentBody(validJson);
		HttpResponse resp = sc.doPost(post);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(validJson, resp.getResponseBody());
	}
	
	@Test
	public void sendInvalidObject() {
		String invalidJson = "{\"key\":\"key\",\"value\":\"value\"}";
		HttpPost post = new HttpPost(sc, "/post");
		post.setContentType(ContentType.APPLICATION_JSON);
		post.setContentBody(invalidJson);
		HttpResponse resp = sc.doPost(post);
		Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatusCode());
	}
}
