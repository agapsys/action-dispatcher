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

package com.agapsys.web;

import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.HttpClient;
import com.agapsys.sevlet.test.HttpGet;
import com.agapsys.sevlet.test.HttpPost;
import com.agapsys.sevlet.test.HttpRequest.HttpHeader;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.web.servlets.LoginServlet;
import com.agapsys.web.servlets.PublicServlet;
import com.agapsys.web.servlets.SecuredServlet;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ActionServletTest {
	// CLASS SCOPE =============================================================	
	public static final String PUBLIC_GET_URL            = "/public/get";
	public static final String PUBLIC_MAPPED_GET_URL     = "/public/mapped/get";
	
	public static final String PUBLIC_POST_URL           = "/public/post";
	public static final String PUBLIC_MAPPED_POST_URL    = "/public/mapped/post";
	
	public static final String SECURED_GET_URL           = "/secured/get";
	public static final String SECURED_MAPPED_GET_URL    = "/secured/mapped/get";
	
	public static final String SECURED_POST_URL          = "/secured/post";
	public static final String SECURED_MAPPED_POST_URL   = "/secured/mapped/post";
	
	public static final String LOGIN_SIMPLE_USER_URL     = "/login/simple";
	public static final String LOGIN_PRIVILDGED_USER_URL = "/login/priviledged";
	
	// INSTANCE SCOPE ==========================================================	
	private ServletContainer sc;
	
	@Before
	public void setUp() {
		// Register dispatcher servlet...
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerServlet(LoginServlet.class);
		context.registerServlet(PublicServlet.class);
		context.registerServlet(SecuredServlet.class);
		
		sc.registerContext(context, "/");
		sc.startServer();
	}
	
	@After
	public void tearDown() {
		sc.stopServer();
	}
	
	
	@Test
	@Ignore
	public void testMappingSlash() {
	}
	
	@Test
	@Ignore
	public void testBeforeAction() {
		
	}
	
	@Test
	@Ignore
	public void testAfterAction() {
		
	}
	
	@Test
	@Ignore
	public void testNotFoundAction() {
		
	}
	
	@Test
	public void testPublicActions() {
		HttpResponse resp;
		HttpHeader csrfHeader;
		
		// GET: PUBLIC GET
		resp = sc.doGet(PUBLIC_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_GET_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		
		// GET: PUBLIC MAPPED GET
		resp = sc.doGet(PUBLIC_MAPPED_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_GET_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		
		// POST: PUBLIC POST		
		resp = sc.doPost(new HttpPost(sc, PUBLIC_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_POST_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		
		// POST: PUBLIC MAPPED POST		
		resp = sc.doPost(new HttpPost(sc, PUBLIC_MAPPED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_POST_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		
		// GET: PUBLIC POST
		resp = sc.doGet(PUBLIC_POST_URL);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// GET: PUBLIC MAPPED POST
		resp = sc.doGet(PUBLIC_MAPPED_POST_URL);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// POST: PUBLIC GET
		resp = sc.doPost(new HttpPost(sc, PUBLIC_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// POST: PUBLIC MAPPED GET
		resp = sc.doPost(new HttpPost(sc, PUBLIC_MAPPED_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
	}
	
	@Test
	public void testForbiddenSecuredActions() {
		HttpResponse resp;
		
		// GET: SECURED GET
		resp = sc.doGet(SECURED_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
		
		// GET: SECURED MAPPED GET
		resp = sc.doGet(SECURED_MAPPED_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
		
		// POST: SECURED POST
		resp = sc.doPost(new HttpPost(sc, SECURED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
		
		// POST: SECURED MAPPED POST
		resp = sc.doPost(new HttpPost(sc, SECURED_MAPPED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
		
		
		// GET: SECURED POST
		resp = sc.doGet(SECURED_POST_URL);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// GET: SECURED MAPPED POST
		resp = sc.doGet(SECURED_MAPPED_POST_URL);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		
		// POST: SECURED GET
		resp = sc.doPost(new HttpPost(sc, SECURED_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// POST: SECURED MAPPED GET
		resp = sc.doPost(new HttpPost(sc, SECURED_MAPPED_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
	}

	@Test
	public void testLoggingUsers() {
		HttpResponse resp;
		
		resp = sc.doGet(LOGIN_SIMPLE_USER_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertNotNull(resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER));
		Assert.assertEquals(LOGIN_SIMPLE_USER_URL, resp.getResponseBody());
		
		resp = sc.doGet(LOGIN_PRIVILDGED_USER_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertNotNull(resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER));
		Assert.assertEquals(LOGIN_PRIVILDGED_USER_URL, resp.getResponseBody());
	}
	
	@Test
	public void testAccessWithLoggedUsers() {
		HttpClient simpleClient;
		HttpClient priviledgedClient;
		
		HttpResponse resp;
		
		HttpHeader simpleCsrfHeader;
		HttpHeader priviledgedCsrfHeader;
		
		HttpGet simpleSecuredGet;
		HttpGet priviledgedSecuredGet;
		
		HttpPost simpleSecuredPost;
		HttpPost priviledgedSecuredPost;
		
		// SIMPLE USER WITH CSRF -----------------------------------------------
		// Logging in...
		simpleClient = new HttpClient();
		resp = sc.doGet(simpleClient, LOGIN_SIMPLE_USER_URL);
		simpleCsrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		
		// GET: SECURED GET
		simpleSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		simpleSecuredGet.addHeaders(simpleCsrfHeader);
		resp = sc.doGet(simpleClient, simpleSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // SECURED GET requires a role
		
		// GET: SECURED MAPPED GET
		simpleSecuredGet = new HttpGet(sc, SECURED_MAPPED_GET_URL);
		simpleSecuredGet.addHeaders(simpleCsrfHeader);
		resp = sc.doGet(simpleClient, simpleSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // SECURED MAPPED GET requires a role
		
		// POST: SECURED POST
		simpleSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		simpleSecuredPost.addHeaders(simpleCsrfHeader);
		resp = sc.doPost(simpleClient, simpleSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());  // SECURED POST requires a role
		
		// POST: SECURED MAPPED POST
		simpleSecuredPost = new HttpPost(sc, SECURED_MAPPED_POST_URL);
		simpleSecuredPost.addHeaders(simpleCsrfHeader);
		resp = sc.doPost(simpleClient, simpleSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());  // SECURED MAPPED POST requires a role
		
		
		// PRIVILEDGED USER WITHOUT CSRF ---------------------------------------
		// Logging in...
		priviledgedClient = new HttpClient();
		resp = sc.doGet(priviledgedClient, LOGIN_PRIVILDGED_USER_URL);
		priviledgedCsrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_MAPPED_GET_URL);
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		
		// POST: SECURED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_MAPPED_POST_URL);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		
		// PRIVILEDGED USER WITH CSRF ------------------------------------------
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		priviledgedSecuredGet.addHeaders(priviledgedCsrfHeader);
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_GET_URL, resp.getResponseBody());
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_MAPPED_GET_URL);
		priviledgedSecuredGet.addHeaders(priviledgedCsrfHeader);
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_GET_URL, resp.getResponseBody());
		
		// POST: SECURED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		priviledgedSecuredPost.addHeaders(priviledgedCsrfHeader);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_POST_URL, resp.getResponseBody());
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_MAPPED_POST_URL);
		priviledgedSecuredPost.addHeaders(priviledgedCsrfHeader);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_POST_URL, resp.getResponseBody());
	}
	// =========================================================================
}
