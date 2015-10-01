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
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.sevlet.test.StacktraceErrorHandler;
import com.agapsys.web.action.dispatcher.servlets.TransactionServlet;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JpaTransactionServletTest {
	// CLASS SCOPE =============================================================
	public static final String JPA_SERVLET_URL  = "/jpa";
	public static final String JPA_COMMIT_URL   = JPA_SERVLET_URL + "/commit";
	public static final String JPA_ROLLBACK_URL = JPA_SERVLET_URL + "/rollback";
	public static final String JPA_COUNT_URL    = JPA_SERVLET_URL + "/count";
	public static final String JPA_CLEAR_URL    = JPA_SERVLET_URL + "/clear";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private static ServletContainer sc;
	
	@BeforeClass
	public static void beforeClass() {
		sc = new ServletContainer();
		ApplicationContext app = new ApplicationContext();
		app.setErrorHandler(new StacktraceErrorHandler());
		app.registerServlet(TransactionServlet.class);
		sc.registerContext(app);
		sc.startServer();
	}
	
	@AfterClass
	public static void after() {
		sc.stopServer();
	}
	
	@Test
	public void testCommit() {
		HttpResponse resp;
		
		resp = sc.doGet(JPA_CLEAR_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		
		resp = sc.doGet(JPA_COUNT_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals("0", resp.getResponseBody());
		
		
		resp = sc.doGet(JPA_COMMIT_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		
		Assert.assertTrue(TransactionServlet.postCommitted);
		
		resp = sc.doGet(JPA_COUNT_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals("100", resp.getResponseBody());
		
		Assert.assertFalse(TransactionServlet.postCommitted);
	}
	
	@Test
	public void testRollback() {
		HttpResponse resp;
		
		resp = sc.doGet(JPA_CLEAR_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		
		resp = sc.doGet(JPA_COUNT_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals("0", resp.getResponseBody());
		
		
		resp = sc.doGet(JPA_COMMIT_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());

		Assert.assertTrue(TransactionServlet.postCommitted);
		
		resp = sc.doGet(JPA_COUNT_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals("100", resp.getResponseBody());
		
		resp = sc.doGet(JPA_ROLLBACK_URL);
		System.out.println(resp.getResponseBody());
		
		resp = sc.doGet(JPA_COUNT_URL);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals("100", resp.getResponseBody());
		
		Assert.assertFalse(TransactionServlet.postCommitted);
	}
	// =========================================================================
}
