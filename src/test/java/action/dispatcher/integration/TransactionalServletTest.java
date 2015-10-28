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

package action.dispatcher.integration;

import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.sevlet.test.StacktraceErrorHandler;
import action.dispatcher.integration.servlets.TransactionalTestServlet;
import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse.StringResponse;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionalServletTest {
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
		app.registerServlet(TransactionalTestServlet.class);
		sc.registerContext(app);
		sc.startServer();
	}
	
	@AfterClass
	public static void after() {
		sc.stopServer();
	}
	
	@Test
	public void testCommit() {
		StringResponse resp;
		
		resp = sc.doRequest(new HttpGet(JPA_CLEAR_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		
		resp = sc.doRequest(new HttpGet(JPA_COUNT_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals("0", resp.getContentString());
		
		// Test post events ----------------------------------------------------
		resp = sc.doRequest(new HttpGet(JPA_COMMIT_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		
		Assert.assertTrue(TransactionalTestServlet.postCommitted);
		Assert.assertFalse(TransactionalTestServlet.postRollbacked);
		// ---------------------------------------------------------------------
		
		resp = sc.doRequest(new HttpGet(JPA_COUNT_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals("100", resp.getContentString());
		
		Assert.assertFalse(TransactionalTestServlet.postCommitted);
		Assert.assertFalse(TransactionalTestServlet.postRollbacked);
	}
	
	@Test
	public void testRollback() {
		StringResponse resp;
		
		resp = sc.doRequest(new HttpGet(JPA_CLEAR_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		
		resp = sc.doRequest(new HttpGet(JPA_COUNT_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals("0", resp.getContentString());
		
		// Test commit ---------------------------------------------------------
		resp = sc.doRequest(new HttpGet(JPA_COMMIT_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());

		Assert.assertTrue(TransactionalTestServlet.postCommitted);
		Assert.assertFalse(TransactionalTestServlet.postRollbacked);
		// ---------------------------------------------------------------------
		
		resp = sc.doRequest(new HttpGet(JPA_COUNT_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals("100", resp.getContentString());
		
		// Test rollback -------------------------------------------------------
		resp = sc.doRequest(new HttpGet(JPA_ROLLBACK_URL));
		System.out.println(resp.getContentString());
		
		Assert.assertFalse(TransactionalTestServlet.postCommitted);
		Assert.assertTrue(TransactionalTestServlet.postRollbacked);
		// ---------------------------------------------------------------------
		
		resp = sc.doRequest(new HttpGet(JPA_COUNT_URL));
		System.out.println(resp.getContentString());
		Assert.assertEquals("100", resp.getContentString());
		
		Assert.assertFalse(TransactionalTestServlet.postCommitted);
		Assert.assertFalse(TransactionalTestServlet.postRollbacked);
	}
	// =========================================================================
}
