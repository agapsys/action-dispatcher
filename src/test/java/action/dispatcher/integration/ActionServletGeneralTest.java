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
import com.agapsys.web.action.dispatcher.ActionServlet;
import com.agapsys.web.action.dispatcher.CsrfSecurityManager;
import com.agapsys.web.action.dispatcher.WebAction;
import action.dispatcher.integration.servlets.DefaultActionServlet;
import action.dispatcher.integration.servlets.LoginServlet;
import action.dispatcher.integration.servlets.PhaseActionsServlet;
import action.dispatcher.integration.servlets.PublicServlet;
import action.dispatcher.integration.servlets.SecuredServlet;
import com.agapsys.http.HttpClient;
import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpHeader;
import com.agapsys.http.HttpRequest;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.http.StringEntityRequest.StringEntityPost;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActionServletGeneralTest {
	// CLASS SCOPE =============================================================
	@WebServlet("/invalid1")
	public static class InvalidUrlPatternServlet extends ActionServlet {
		@WebAction
		public void get(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {}
	}
	
	// Default actions ---------------------------------------------------------
	public static final String DEFAULT_ACTION_DEFAULT_URL = "/default";
	public static final String DEFAULT_ACTION_GET_URL     = "/default/get";
	public static final String DEFAULT_ACTION_POST_URL    = "/default/post";
	
	// Phase actions -----------------------------------------------------------	
	public static final String PHASE_DEFAULT_URL       = "/phase";
	public static final String PHASE_BEFORE_HEADER     = "before-header";
	public static final String PHASE_AFTER_HEADER      = "before-header";
	public static final String PHASE_NOT_FOUND_HEADER  = "not-found";
	
	// Secured actions ---------------------------------------------------------	
	public static final String PUBLIC_DEFAULT                   = "/public";
	public static final String PUBLIC_GET_URL                   = "/public/get";
	public static final String PUBLIC_MAPPED_GET_URL            = "/public/mapped/get";
	public static final String PUBLIC_MAPPED_WITH_SLASH_GET_URL = "/public/mapped/get2";
	public static final String PUBLIC_POST_URL                  = "/public/post";
	public static final String PUBLIC_MAPPED_POST_URL           = "/public/mapped/post";
	public static final String PUBLIC_WEBACTIONS_URL            = "/public/repeatableGetOrPost";
	public static final String PUBLIC_MULTIPLE_METHODS_URL      = "/public/multipleMethods";
	
	public static final String SECURED_DEFAULT_URL       = "/secured";
	public static final String SECURED_GET_URL           = "/secured/get";
	public static final String SECURED_MAPPED_GET_URL    = "/secured/mapped/get";
	public static final String SECURED_POST_URL          = "/secured/post";
	public static final String SECURED_MAPPED_POST_URL   = "/secured/mapped/post";
	
	public static final String LOGIN_SIMPLE_USER_URL     = "/login/simple";
	public static final String LOGIN_PRIVILDGED_USER_URL = "/login/priviledged";
	public static final String LOGIN_ADMIN_USER_URL     = "/login/admin";
	
	// INSTANCE SCOPE ==========================================================	
	private ServletContainer sc;
	
	private void expectNullPhaseHeaders(StringResponse resp) {
		Assert.assertNull(resp.getFirstHeader(PHASE_BEFORE_HEADER));
		Assert.assertNull(resp.getFirstHeader(PHASE_AFTER_HEADER));
	}
	
	@Before
	public void setUp() {
		// Register dispatcher servlet...
		sc = new ServletContainer() {

			@Override
			public StringResponse doRequest(HttpClient client, HttpRequest request) {
				StringResponse resp = super.doRequest(client, request);
				
				if (resp.getStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
					System.out.println(resp.getContentString());
				
				return resp;
			}
		};
		
		ApplicationContext context = new ApplicationContext();
		context.setErrorHandler(new StacktraceErrorHandler());
		
		context.registerServlet(InvalidUrlPatternServlet.class);
		context.registerServlet(LoginServlet.class);
		context.registerServlet(PublicServlet.class);
		context.registerServlet(SecuredServlet.class);
		context.registerServlet(PhaseActionsServlet.class);
		context.registerServlet(DefaultActionServlet.class);
		
		sc.registerContext(context, "/");
		sc.startServer();
	}
	
	@After
	public void tearDown() {
		sc.stopServer();
	}
	
	@Test
	public void testDefaultActions() {
		StringResponse resp;
		
		// GET: GET
		resp = sc.doRequest(new HttpGet(DEFAULT_ACTION_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_GET_URL, resp.getContentString());
		
		
		// POST: POST		
		resp = sc.doRequest(new StringEntityPost(DEFAULT_ACTION_POST_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_POST_URL, resp.getContentString());
		
		// GET: DEFAULT
		resp = sc.doRequest(new HttpGet(DEFAULT_ACTION_DEFAULT_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_GET_URL, resp.getContentString());
		
		// GET: DEFAULT + "/"
		resp = sc.doRequest(new HttpGet(DEFAULT_ACTION_DEFAULT_URL + "/"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_GET_URL, resp.getContentString());
		
		// POST: DEFAULT
		resp = sc.doRequest(new StringEntityPost(DEFAULT_ACTION_DEFAULT_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_POST_URL, resp.getContentString());
		
		// POST: DEFAULT + "/"
		resp = sc.doRequest(new StringEntityPost(DEFAULT_ACTION_DEFAULT_URL + "/", "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_POST_URL, resp.getContentString());
	}
	
	@Test
	public void testMappingSlash() {
		StringResponse resp;
		HttpHeader csrfHeader;
		
		// GET: PUBLIC GET
		resp = sc.doRequest(new HttpGet(PUBLIC_MAPPED_WITH_SLASH_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_WITH_SLASH_GET_URL, resp.getContentString());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
	}
	
	@Test
	public void testPhaseActions() {
		StringResponse resp;
		HttpHeader beforeHeader;
		HttpHeader afterHeader;
		HttpHeader notFoundHeader;
		
		// GET
		resp = sc.doRequest(new HttpGet(PHASE_DEFAULT_URL));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);
		
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PHASE_DEFAULT_URL, resp.getContentString());
		
		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());
		
		
		// POST
		resp = sc.doRequest(new StringEntityPost(PHASE_DEFAULT_URL, "text/plain", "UTF-8"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PHASE_DEFAULT_URL, resp.getContentString());
		
		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());
		
		// GET: NOT FOUND
		resp = sc.doRequest(new HttpGet(PHASE_DEFAULT_URL + "/unknown"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);
		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		Assert.assertEquals("", resp.getContentString());
		
		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
		
		// POST: NOT FOUND
		resp = sc.doRequest(new StringEntityPost(PHASE_DEFAULT_URL + "/unknown", "text/plain", "UTF-8"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);
		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		Assert.assertEquals("", resp.getContentString());
		
		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
	}
	
	@Test
	public void testPublicActions() {
		StringResponse resp;
		HttpHeader csrfHeader;
		
		// GET: PUBLIC GET
		resp = sc.doRequest(new HttpGet(PUBLIC_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_GET_URL, resp.getContentString());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
		
		// GET: PUBLIC MAPPED GET
		resp = sc.doRequest(new HttpGet(PUBLIC_MAPPED_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_GET_URL, resp.getContentString());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
		
		// POST: PUBLIC POST		
		resp = sc.doRequest(new StringEntityPost(PUBLIC_POST_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_POST_URL, resp.getContentString());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
		
		// POST: PUBLIC MAPPED POST		
		resp = sc.doRequest(new StringEntityPost(PUBLIC_MAPPED_POST_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_POST_URL, resp.getContentString());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
		
		// GET: PUBLIC POST
		resp = sc.doRequest(new HttpGet(PUBLIC_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// GET: PUBLIC MAPPED POST
		resp = sc.doRequest(new HttpGet(PUBLIC_MAPPED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// POST: PUBLIC GET
		resp = sc.doRequest(new StringEntityPost(PUBLIC_GET_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// POST: PUBLIC MAPPED GET
		resp = sc.doRequest(new StringEntityPost(PUBLIC_MAPPED_GET_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
	}
	
	@Test
	public void testPublicRepeatble() {
		StringResponse resp;
		
		// Multiple @WebAction's...
		// GET:
		resp = sc.doRequest(new HttpGet(PUBLIC_WEBACTIONS_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_WEBACTIONS_URL + "GET", resp.getContentString());
		
		// POST:
		resp = sc.doRequest(new StringEntityPost(PUBLIC_WEBACTIONS_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_WEBACTIONS_URL + "POST", resp.getContentString());
		
		
		// Multiple methods, same @WebAction...
		// GET:
		resp = sc.doRequest(new HttpGet(PUBLIC_MULTIPLE_METHODS_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MULTIPLE_METHODS_URL + "GET", resp.getContentString());
		
		// POST:
		resp = sc.doRequest(new StringEntityPost(PUBLIC_MULTIPLE_METHODS_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MULTIPLE_METHODS_URL + "POST", resp.getContentString());
	}
	
	@Test
	public void testForbiddenSecuredActions() {
		StringResponse resp;
		
		// GET: SECURED GET
		resp = sc.doRequest(new HttpGet(SECURED_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		// GET: SECURED MAPPED GET
		resp = sc.doRequest(new HttpGet(SECURED_MAPPED_GET_URL));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		// POST: SECURED POST
		resp = sc.doRequest(new StringEntityPost(SECURED_POST_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		// POST: SECURED MAPPED POST
		resp = sc.doRequest(new StringEntityPost(SECURED_MAPPED_POST_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		
		// GET: SECURED POST
		resp = sc.doRequest(new HttpGet(SECURED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// GET: SECURED MAPPED POST
		resp = sc.doRequest(new HttpGet(SECURED_MAPPED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		
		// POST: SECURED GET
		resp = sc.doRequest(new StringEntityPost(SECURED_GET_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		
		// POST: SECURED MAPPED GET
		resp = sc.doRequest(new StringEntityPost(SECURED_MAPPED_GET_URL, "text/plain", "UTF-8"));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
	}

	@Test
	public void testLoggingUsers() {
		StringResponse resp;
		
		resp = sc.doRequest(new HttpGet(LOGIN_SIMPLE_USER_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertNotNull(resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER));
		Assert.assertEquals(LOGIN_SIMPLE_USER_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		
		resp = sc.doRequest(new HttpGet(LOGIN_PRIVILDGED_USER_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertNotNull(resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER));
		Assert.assertEquals(LOGIN_PRIVILDGED_USER_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		resp = sc.doRequest(new HttpGet(LOGIN_ADMIN_USER_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertNotNull(resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER));
		Assert.assertEquals(LOGIN_ADMIN_USER_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
	}
	
	@Test
	public void testAccessWithLoggedUsers() {
		HttpHeader csrfHeader;
				
		HttpClient simpleClient;
		HttpClient priviledgedClient;
		HttpClient adminClient;
		
		StringResponse resp;
		
		HttpGet simpleSecuredGet;
		HttpGet priviledgedSecuredGet;
		
		StringEntityPost simpleSecuredPost;
		StringEntityPost priviledgedSecuredPost;
		
		// SIMPLE USER WITH CSRF -----------------------------------------------
		// Logging in...
		simpleClient = new HttpClient();
		resp = sc.doRequest(simpleClient, new HttpGet(LOGIN_SIMPLE_USER_URL));
		simpleClient.addDefaultHeaders(resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER)); // <-- Adds CSRF token to default headers
		
		// GET: SECURED GET
		simpleSecuredGet = new HttpGet(SECURED_GET_URL);
		resp = sc.doRequest(simpleClient, simpleSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // SECURED GET requires a role
		
		// GET: SECURED MAPPED GET
		simpleSecuredGet = new HttpGet(SECURED_MAPPED_GET_URL);
		resp = sc.doRequest(simpleClient, simpleSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // SECURED MAPPED GET requires a role
		
		// POST: SECURED POST
		simpleSecuredPost = new StringEntityPost(SECURED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(simpleClient, simpleSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());  // SECURED POST requires a role
		
		// POST: SECURED MAPPED POST
		simpleSecuredPost = new StringEntityPost(SECURED_MAPPED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(simpleClient, simpleSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());  // SECURED MAPPED POST requires a role
		
		// PRIVILEDGED USER WITHOUT CSRF ---------------------------------------
		// Logging in...
		priviledgedClient = new HttpClient();
		resp = sc.doRequest(priviledgedClient, new HttpGet(LOGIN_PRIVILDGED_USER_URL));
		csrfHeader = resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER); // Stores CSRF header for later usage
		
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(SECURED_GET_URL);
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(SECURED_MAPPED_GET_URL);
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// POST: SECURED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_MAPPED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// PRIVILEDGED USER WITH CSRF ------------------------------------------
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(SECURED_GET_URL);
		priviledgedClient.addDefaultHeaders(csrfHeader); // <-- Adds CSRF token to default headers
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_GET_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(SECURED_MAPPED_GET_URL);
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_GET_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_POST_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_MAPPED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_POST_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		// ADMIN USER WITHOUT CSRF -----------------------------------------------
		// Logging in...
		adminClient = new HttpClient();
		resp = sc.doRequest(adminClient, new HttpGet(LOGIN_ADMIN_USER_URL));
		csrfHeader = resp.getFirstHeader(CsrfSecurityManager.CSRF_HEADER); // Stores CSRF header for later usage
		
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(SECURED_GET_URL);
		resp = sc.doRequest(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(SECURED_MAPPED_GET_URL);
		resp = sc.doRequest(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// POST: SECURED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_MAPPED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// ADMIN USER WITH CSRF ------------------------------------------
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(SECURED_GET_URL);
		adminClient.addDefaultHeaders(csrfHeader); // <-- Adds CSRF token to default headers
		resp = sc.doRequest(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_GET_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(SECURED_MAPPED_GET_URL);
		resp = sc.doRequest(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_GET_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_POST_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new StringEntityPost(SECURED_MAPPED_POST_URL, "text/plain", "UTF-8");
		resp = sc.doRequest(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_POST_URL, resp.getContentString());
		expectNullPhaseHeaders(resp);
	}
	// =========================================================================
}
