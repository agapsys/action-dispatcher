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
import com.agapsys.sevlet.test.HttpRequest;
import com.agapsys.sevlet.test.HttpRequest.HttpHeader;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActionDispatcherServletTest {
	// CLASS SCOPE =============================================================
	private static String TEST_ROLE = "test";
	
	private static final String PUBLIC_GET_URL  = "/public/get";
	private static final String PUBLIC_POST_URL = "/public/post";
	
	private static final String SECURED_GET_URL  = "/protected/get";
	private static final String SECURED_POST_URL = "/protected/post";
	
	private static final String LOGIN_SIMPLE_USER_URL = "/login/simple";
	private static final String LOGIN_PRIVILDGED_USER_URL = "/login/priviledged";
	
	private static class PublicAction implements Action {
		@Override
		public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().print(this.getClass().getSimpleName());
		}
	}
	private static class PublicPostAction extends PublicAction {}
	private static class PublicGetAction extends PublicAction {}
	
	private static class SimpleUser implements User {
		private final Set<String> roles = new LinkedHashSet<>();
		
		@Override
		public Set<String> getRoles() {
			return roles;
		}
	}
	private static class PriviledgedUser extends SimpleUser {
		public PriviledgedUser() {
			super.getRoles().add(TEST_ROLE);
		}
	}
	
	private static class LoginSimpleUserAction extends SecuredAction {
		private final SimpleUser simpleUser = new SimpleUser();
		
		@Override
		protected void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			setSessionUser(req, resp, simpleUser);
		}
	}
	private static class LoginPriviledgedUserAction extends SecuredAction {
		private final PriviledgedUser priviledgedUser = new PriviledgedUser();

		@Override
		protected void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			setSessionUser(req, resp, priviledgedUser);
		}
		
		
	}
	
	private static class SimpleSecuredAction extends SecuredAction {
		public SimpleSecuredAction() {
			super(TEST_ROLE);
		}
		
		@Override
		protected void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().print(this.getClass().getSimpleName());
		}
	}
	private static class SecuredGetAction extends SimpleSecuredAction {}
	private static class SecuredPostAction extends SimpleSecuredAction {}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================	
	private ServletContainer sc;
	
	@Before
	public void setUp() {
		// Register actions...
		ActionDispatcherServlet.registerAction(new LoginSimpleUserAction(),      HttpMethod.GET, LOGIN_SIMPLE_USER_URL);
		ActionDispatcherServlet.registerAction(new LoginPriviledgedUserAction(), HttpMethod.GET, LOGIN_PRIVILDGED_USER_URL);
		
		ActionDispatcherServlet.registerAction(new PublicGetAction(),  HttpMethod.GET,  PUBLIC_GET_URL);
		ActionDispatcherServlet.registerAction(new PublicPostAction(), HttpMethod.POST, PUBLIC_POST_URL);
		
		ActionDispatcherServlet.registerAction(new SecuredGetAction(),  HttpMethod.GET,  SECURED_GET_URL);
		ActionDispatcherServlet.registerAction(new SecuredPostAction(), HttpMethod.POST, SECURED_POST_URL);
		
		// Register dispatcher servlet...
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerServlet(ActionDispatcherServlet.class);
		
		sc.registerContext(context, "/");
		sc.startServer();
	}
	
	@After
	public void tearDown() {
		ActionDispatcherServlet.clearActions();
		sc.stopServer();
	}
	
	@Test
	public void testPublicActions() {
		HttpResponse resp = sc.doGet(PUBLIC_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PublicGetAction.class.getSimpleName(), resp.getResponseBody());
		
		HttpHeader csrfHeader = resp.getFirstHeader(SecuredAction.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		
		resp = sc.doGet(PUBLIC_POST_URL);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		resp = sc.doPost(new HttpPost(sc, PUBLIC_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PublicPostAction.class.getSimpleName(), resp.getResponseBody());
		
		resp = sc.doPost(new HttpPost(sc, PUBLIC_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
	}
	
	@Test
	public void testForbiddenSecuredActions() {
		HttpResponse resp = sc.doGet(SECURED_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
		
		resp = sc.doPost(new HttpPost(sc, SECURED_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		
		resp = sc.doPost(new HttpPost(sc, SECURED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
		
		resp = sc.doGet(SECURED_POST_URL);
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
	}

	@Test
	public void testLoggingUsers() {
		HttpResponse resp = sc.doGet(LOGIN_SIMPLE_USER_URL);
		Assert.assertNotNull(resp.getFirstHeader(SecuredAction.CSRF_HEADER));
	}
	
	@Test
	public void testAccessWithLoggedUsers() {
		HttpClient simpleClient;
		HttpClient priviledgedClient;
		
		HttpResponse resp;
		HttpHeader csrfHeader;
		
		HttpGet simpleSecuredGet;
		HttpGet priviledgedSecuredGet;
		
		HttpPost simpleSecuredPost;
		HttpPost priviledgedSecuredPost;
		
		// SIMPLE USER ---------------------------------------------------------
		// Logging in...
		simpleClient = new HttpClient();
		resp = sc.doGet(simpleClient, LOGIN_SIMPLE_USER_URL);
		csrfHeader = resp.getFirstHeader(SecuredAction.CSRF_HEADER);
		Assert.assertNotNull(csrfHeader);
		
		// SECURED GET
		simpleSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		simpleSecuredGet.addHeaders(csrfHeader);
		resp = sc.doGet(simpleClient, simpleSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // SECURED_GET_URL requires TEST role
		
		// SECURED POST
		simpleSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		simpleSecuredPost.addHeaders(csrfHeader);
		resp = sc.doPost(simpleClient, simpleSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());  // SECURED_POST_URL requires TEST role
		
		
		// PRIVILEDGED USER ----------------------------------------------------
		// Logging in...
		priviledgedClient = new HttpClient();
		resp = sc.doGet(priviledgedClient, LOGIN_PRIVILDGED_USER_URL);
		csrfHeader = resp.getFirstHeader(SecuredAction.CSRF_HEADER);
		Assert.assertNotNull(csrfHeader);
		
		// === SECURED GET: PRIVILEDGED USER ===
		priviledgedSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		priviledgedSecuredGet.addHeaders(csrfHeader);
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SecuredGetAction.class.getSimpleName(), resp.getResponseBody());
		
		// === SECURED POST: SIMPLE USER ===
		priviledgedSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		priviledgedSecuredPost.addHeaders(csrfHeader);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SecuredPostAction.class.getSimpleName(), resp.getResponseBody());	
	}
	// =========================================================================
}
