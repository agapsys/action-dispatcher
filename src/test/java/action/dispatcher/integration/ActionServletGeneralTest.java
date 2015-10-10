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
import com.agapsys.sevlet.test.HttpClient;
import com.agapsys.sevlet.test.HttpGet;
import com.agapsys.sevlet.test.HttpPost;
import com.agapsys.sevlet.test.HttpRequest;
import com.agapsys.sevlet.test.HttpRequest.HttpHeader;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.sevlet.test.StacktraceErrorHandler;
import com.agapsys.web.action.dispatcher.ActionServlet;
import com.agapsys.web.action.dispatcher.CsrfSecurityHandler;
import com.agapsys.web.action.dispatcher.WebAction;
import action.dispatcher.integration.servlets.DefaultActionServlet;
import action.dispatcher.integration.servlets.LoginServlet;
import action.dispatcher.integration.servlets.PhaseActionsServlet;
import action.dispatcher.integration.servlets.PublicServlet;
import action.dispatcher.integration.servlets.SecuredServlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
	public static final String PUBLIC_REPEATABLE_GET_POST_URL   = "/public/repeatableGetOrPost";
	
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
	
	private void expectNullPhaseHeaders(HttpResponse resp) {
		Assert.assertNull(resp.getFirstHeader(PHASE_BEFORE_HEADER));
		Assert.assertNull(resp.getFirstHeader(PHASE_AFTER_HEADER));
	}
	
	@Before
	public void setUp() {
		// Register dispatcher servlet...
		sc = new ServletContainer() {

			@Override
			protected HttpResponse doRequest(HttpClient client, HttpRequest request) throws IllegalArgumentException, RuntimeException {
				HttpResponse resp = super.doRequest(client, request);
				
				if (resp.getStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
					System.out.println(resp.getResponseBody());
				
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
	public void callInvalidMappedServlet() {
		HttpResponse resp = sc.doGet("/invalid1");
		Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp.getStatusCode());
		String expectedErrorMessage = String.format("Invalid URL pattern '%s' for class '%s' (pattern must end with '/*')", "/invalid1", InvalidUrlPatternServlet.class.getName());
		Assert.assertTrue(resp.getResponseBody().contains(expectedErrorMessage));
	}
	
	@Test
	public void testDefaultActions() {
		HttpResponse resp;
		
		// GET: GET
		resp = sc.doGet(DEFAULT_ACTION_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_GET_URL, resp.getResponseBody());
		
		
		// POST: POST
		resp = sc.doPost(new HttpPost(sc, DEFAULT_ACTION_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_POST_URL, resp.getResponseBody());
		
		// GET: DEFAULT
		resp = sc.doGet(DEFAULT_ACTION_DEFAULT_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_GET_URL, resp.getResponseBody());
		
		// GET: DEFAULT + "/"
		resp = sc.doGet(DEFAULT_ACTION_DEFAULT_URL + "/");
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_GET_URL, resp.getResponseBody());
		
		// POST: DEFAULT
		resp = sc.doPost(new HttpPost(sc, DEFAULT_ACTION_DEFAULT_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_POST_URL, resp.getResponseBody());
		
		// POST: DEFAULT + "/"
		resp = sc.doPost(new HttpPost(sc, DEFAULT_ACTION_DEFAULT_URL + "/"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(DEFAULT_ACTION_POST_URL, resp.getResponseBody());
	}
	
	@Test
	public void testMappingSlash() {
		HttpResponse resp;
		HttpHeader csrfHeader;
		
		// GET: PUBLIC GET
		resp = sc.doGet(PUBLIC_MAPPED_WITH_SLASH_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_WITH_SLASH_GET_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
	}
	
	@Test
	public void testPhaseActions() {
		HttpResponse resp;
		HttpHeader beforeHeader;
		HttpHeader afterHeader;
		HttpHeader notFoundHeader;
		
		// GET
		resp = sc.doGet(PHASE_DEFAULT_URL);
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);
		
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PHASE_DEFAULT_URL, resp.getResponseBody());
		
		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());
		
		
		// POST
		resp = sc.doPost(new HttpPost(sc, PHASE_DEFAULT_URL));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PHASE_DEFAULT_URL, resp.getResponseBody());
		
		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());
		
		// GET: NOT FOUND
		resp = sc.doGet(PHASE_DEFAULT_URL + "/unknown");
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);
		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		Assert.assertEquals("", resp.getResponseBody());
		
		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
		
		// POST: NOT FOUND
		resp = sc.doPost(new HttpPost(sc, PHASE_DEFAULT_URL + "/unknown"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);
		
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.getStatusCode());
		Assert.assertEquals("", resp.getResponseBody());
		
		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);
		
		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
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
		expectNullPhaseHeaders(resp);
		
		// GET: PUBLIC MAPPED GET
		resp = sc.doGet(PUBLIC_MAPPED_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_GET_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
		
		// POST: PUBLIC POST		
		resp = sc.doPost(new HttpPost(sc, PUBLIC_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_POST_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
		
		// POST: PUBLIC MAPPED POST		
		resp = sc.doPost(new HttpPost(sc, PUBLIC_MAPPED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_MAPPED_POST_URL, resp.getResponseBody());
		
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER);
		Assert.assertNull(csrfHeader);
		expectNullPhaseHeaders(resp);
		
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
	public void testPublicRepeatble() {
		HttpResponse resp;
		
		// GET:
		resp = sc.doGet(PUBLIC_REPEATABLE_GET_POST_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_REPEATABLE_GET_POST_URL + "GET", resp.getResponseBody());
		
		// POST:
		resp = sc.doPost(new HttpPost(sc, PUBLIC_REPEATABLE_GET_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(PUBLIC_REPEATABLE_GET_POST_URL + "POST", resp.getResponseBody());
	}
	
	@Test
	public void testForbiddenSecuredActions() {
		HttpResponse resp;
		
		// GET: SECURED GET
		resp = sc.doGet(SECURED_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		// GET: SECURED MAPPED GET
		resp = sc.doGet(SECURED_MAPPED_GET_URL);
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		// POST: SECURED POST
		resp = sc.doPost(new HttpPost(sc, SECURED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		// POST: SECURED MAPPED POST
		resp = sc.doPost(new HttpPost(sc, SECURED_MAPPED_POST_URL));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatusCode());
		
		
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
		expectNullPhaseHeaders(resp);
		
		
		resp = sc.doGet(LOGIN_PRIVILDGED_USER_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertNotNull(resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER));
		Assert.assertEquals(LOGIN_PRIVILDGED_USER_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		resp = sc.doGet(LOGIN_ADMIN_USER_URL);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertNotNull(resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER));
		Assert.assertEquals(LOGIN_ADMIN_USER_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
	}
	
	@Test
	public void testAccessWithLoggedUsers() {
		HttpHeader csrfHeader;
				
		HttpClient simpleClient;
		HttpClient priviledgedClient;
		HttpClient adminClient;
		
		HttpResponse resp;
		
		HttpGet simpleSecuredGet;
		HttpGet priviledgedSecuredGet;
		
		HttpPost simpleSecuredPost;
		HttpPost priviledgedSecuredPost;
		
		// SIMPLE USER WITH CSRF -----------------------------------------------
		// Logging in...
		simpleClient = new HttpClient();
		resp = sc.doGet(simpleClient, LOGIN_SIMPLE_USER_URL);
		simpleClient.addDefaultHeaders(resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER)); // <-- Adds CSRF token to default headers
		
		// GET: SECURED GET
		simpleSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		resp = sc.doGet(simpleClient, simpleSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // SECURED GET requires a role
		
		// GET: SECURED MAPPED GET
		simpleSecuredGet = new HttpGet(sc, SECURED_MAPPED_GET_URL);
		resp = sc.doGet(simpleClient, simpleSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // SECURED MAPPED GET requires a role
		
		// POST: SECURED POST
		simpleSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		resp = sc.doPost(simpleClient, simpleSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());  // SECURED POST requires a role
		
		// POST: SECURED MAPPED POST
		simpleSecuredPost = new HttpPost(sc, SECURED_MAPPED_POST_URL);
		resp = sc.doPost(simpleClient, simpleSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());  // SECURED MAPPED POST requires a role
		
		// PRIVILEDGED USER WITHOUT CSRF ---------------------------------------
		// Logging in...
		priviledgedClient = new HttpClient();
		resp = sc.doGet(priviledgedClient, LOGIN_PRIVILDGED_USER_URL);
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER); // Stores CSRF header for later usage
		
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
		priviledgedClient.addDefaultHeaders(csrfHeader); // <-- Adds CSRF token to default headers
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_GET_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_MAPPED_GET_URL);
		resp = sc.doGet(priviledgedClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_GET_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_POST_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_MAPPED_POST_URL);
		resp = sc.doPost(priviledgedClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_POST_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		// ADMIN USER WITHOUT CSRF -----------------------------------------------
		// Logging in...
		adminClient = new HttpClient();
		resp = sc.doGet(adminClient, LOGIN_ADMIN_USER_URL);
		csrfHeader = resp.getFirstHeader(CsrfSecurityHandler.CSRF_HEADER); // Stores CSRF header for later usage
		
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		resp = sc.doGet(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_MAPPED_GET_URL);
		resp = sc.doGet(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// POST: SECURED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		resp = sc.doPost(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_MAPPED_POST_URL);
		resp = sc.doPost(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode()); // CSRF token header was not sent
		
		// ADMIN USER WITH CSRF ------------------------------------------
		// GET: SECURED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_GET_URL);
		adminClient.addDefaultHeaders(csrfHeader); // <-- Adds CSRF token to default headers
		resp = sc.doGet(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_GET_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		// GET: SECURED MAPPED GET
		priviledgedSecuredGet = new HttpGet(sc, SECURED_MAPPED_GET_URL);
		resp = sc.doGet(adminClient, priviledgedSecuredGet);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_GET_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_POST_URL);
		resp = sc.doPost(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_POST_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
		
		// POST: SECURED MAPPED POST
		priviledgedSecuredPost = new HttpPost(sc, SECURED_MAPPED_POST_URL);
		resp = sc.doPost(adminClient, priviledgedSecuredPost);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(SECURED_MAPPED_POST_URL, resp.getResponseBody());
		expectNullPhaseHeaders(resp);
	}
	// =========================================================================
}
