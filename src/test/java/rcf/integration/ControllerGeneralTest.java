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

package rcf.integration;

import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpHeader;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.http.StringEntityRequest.StringEntityPost;
import com.agapsys.rcf.ControllerRegistrationListener;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.sevlet.container.StacktraceErrorHandler;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcf.ServletContainerBuilder;
import rcf.integration.controllers.Controller1;
import rcf.integration.controllers.Controller2;
import rcf.integration.controllers.DefaultController;
import rcf.integration.controllers.PhaseController;
import rcf.integration.controllers.PublicController;

public class ControllerGeneralTest {
	// STATIC SCOPE ============================================================
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
	
	private static void assertStatus(int expected, StringResponse resp) {
		Assert.assertEquals(expected, resp.getStatusCode());
	}
	
	private static void assertResponseEquals(String expected, StringResponse resp) {
		if (resp.getStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
			throw new RuntimeException("Internal server error");
		
		assertStatus(200, resp);
		
		Assert.assertEquals(String.format("\"%s\"", expected), resp.getContentString());
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private ServletContainer sc1;
	private ServletContainer sc2;

	private void expectNullPhaseHeaders(StringResponse resp) {
		Assert.assertNull(resp.getFirstHeader(PHASE_BEFORE_HEADER));
		Assert.assertNull(resp.getFirstHeader(PHASE_AFTER_HEADER));
	}

	@Before
	public void before() {
		// Register controllers directly...
		sc1 = new ServletContainerBuilder()
			.registerController(PublicController.class)
			.registerController(PhaseController.class)
			.registerController(DefaultController.class)
			.setErrorHandler(new StacktraceErrorHandler())
			.build();

		// Controller registration via listener...
		sc2 = new ServletContainerBuilder()
			.registerEventListener(ControllerRegistrationListener.class)
			.build();
		
		sc1.startServer();
		sc2.startServer();
	}

	@After
	public void after() {
		sc1.stopServer();
		sc2.stopServer();
	}

	@Test
	public void testDefaultActions() {
		StringResponse resp;

		// GET: GET ------------------------------------------------------------
		resp = sc1.doRequest(new HttpGet(DEFAULT_ACTION_GET_URL));
		assertResponseEquals(DEFAULT_ACTION_GET_URL, resp);
		
		resp = sc2.doRequest(new HttpGet(DEFAULT_ACTION_GET_URL));
		assertResponseEquals(DEFAULT_ACTION_GET_URL, resp);
		// ---------------------------------------------------------------------

		// POST: POST ----------------------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", DEFAULT_ACTION_POST_URL));
		assertResponseEquals(DEFAULT_ACTION_POST_URL, resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", DEFAULT_ACTION_POST_URL));
		assertResponseEquals(DEFAULT_ACTION_POST_URL, resp);
		// ---------------------------------------------------------------------

		// GET: DEFAULT
		resp = sc1.doRequest(new HttpGet(DEFAULT_ACTION_DEFAULT_URL));
		assertResponseEquals(DEFAULT_ACTION_GET_URL, resp);
		
		resp = sc2.doRequest(new HttpGet(DEFAULT_ACTION_DEFAULT_URL));
		assertResponseEquals(DEFAULT_ACTION_GET_URL, resp);
		// ---------------------------------------------------------------------
		
		// GET: DEFAULT + "/"
		resp = sc1.doRequest(new HttpGet(DEFAULT_ACTION_DEFAULT_URL + "/"));
		assertResponseEquals(DEFAULT_ACTION_GET_URL, resp);
		
		resp = sc2.doRequest(new HttpGet(DEFAULT_ACTION_DEFAULT_URL + "/"));
		assertResponseEquals(DEFAULT_ACTION_GET_URL, resp);
		// ---------------------------------------------------------------------
		
		// POST: DEFAULT
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", DEFAULT_ACTION_DEFAULT_URL));
		assertResponseEquals(DEFAULT_ACTION_POST_URL, resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", DEFAULT_ACTION_DEFAULT_URL));
		assertResponseEquals(DEFAULT_ACTION_POST_URL, resp);
		// ---------------------------------------------------------------------
		
		// POST: DEFAULT + "/"
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", DEFAULT_ACTION_DEFAULT_URL + "/"));
		assertResponseEquals(DEFAULT_ACTION_POST_URL, resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", DEFAULT_ACTION_DEFAULT_URL + "/"));
		assertResponseEquals(DEFAULT_ACTION_POST_URL, resp);
		// ---------------------------------------------------------------------
	}

	@Test
	public void testMappingSlash() {
		StringResponse resp;

		// GET: PUBLIC GET
		resp = sc1.doRequest(new HttpGet(PUBLIC_MAPPED_WITH_SLASH_GET_URL));
		assertResponseEquals(PUBLIC_MAPPED_WITH_SLASH_GET_URL, resp);
		expectNullPhaseHeaders(resp);
		
		resp = sc2.doRequest(new HttpGet(PUBLIC_MAPPED_WITH_SLASH_GET_URL));
		assertResponseEquals(PUBLIC_MAPPED_WITH_SLASH_GET_URL, resp);
		expectNullPhaseHeaders(resp);
	}

	@Test
	public void testPhaseActions() {
		StringResponse resp;
		HttpHeader beforeHeader;
		HttpHeader afterHeader;
		HttpHeader notFoundHeader;

		// GET -----------------------------------------------------------------
		resp = sc1.doRequest(new HttpGet(PHASE_DEFAULT_URL));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertResponseEquals(PHASE_DEFAULT_URL, resp);

		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);

		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());
		
		resp = sc2.doRequest(new HttpGet(PHASE_DEFAULT_URL));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertResponseEquals(PHASE_DEFAULT_URL, resp);

		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);

		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());
		// ---------------------------------------------------------------------

		// POST ----------------------------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PHASE_DEFAULT_URL));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertResponseEquals(PHASE_DEFAULT_URL, resp);

		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);

		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());

		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PHASE_DEFAULT_URL));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertResponseEquals(PHASE_DEFAULT_URL, resp);

		Assert.assertNotNull(beforeHeader);
		Assert.assertNotNull(afterHeader);
		Assert.assertNull(notFoundHeader);

		Assert.assertEquals(PHASE_BEFORE_HEADER, beforeHeader.getValue());
		Assert.assertEquals(PHASE_AFTER_HEADER, afterHeader.getValue());
		// ---------------------------------------------------------------------
		
		// GET: NOT FOUND
		resp = sc1.doRequest(new HttpGet(PHASE_DEFAULT_URL + "/unknown"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);

		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);

		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
		
		resp = sc2.doRequest(new HttpGet(PHASE_DEFAULT_URL + "/unknown"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);

		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);

		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
		// ---------------------------------------------------------------------
		
		// POST: NOT FOUND -----------------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PHASE_DEFAULT_URL + "/unknown"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);

		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);

		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PHASE_DEFAULT_URL + "/unknown"));
		beforeHeader = resp.getFirstHeader(PHASE_BEFORE_HEADER);
		afterHeader = resp.getFirstHeader(PHASE_AFTER_HEADER);
		notFoundHeader = resp.getFirstHeader(PHASE_NOT_FOUND_HEADER);

		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);

		Assert.assertNull(beforeHeader);
		Assert.assertNull(afterHeader);
		Assert.assertNotNull(notFoundHeader);

		Assert.assertEquals(PHASE_NOT_FOUND_HEADER, notFoundHeader.getValue());
		// ---------------------------------------------------------------------
	}

	@Test
	public void testPublicActions() {
		StringResponse resp;

		// GET: PUBLIC GET -----------------------------------------------------
		resp = sc1.doRequest(new HttpGet(PUBLIC_GET_URL));
		assertResponseEquals(PUBLIC_GET_URL, resp);
		expectNullPhaseHeaders(resp);
		
		resp = sc2.doRequest(new HttpGet(PUBLIC_GET_URL));
		assertResponseEquals(PUBLIC_GET_URL, resp);
		expectNullPhaseHeaders(resp);
		// ---------------------------------------------------------------------

		// GET: PUBLIC MAPPED GET ----------------------------------------------
		resp = sc1.doRequest(new HttpGet(PUBLIC_MAPPED_GET_URL));
		assertResponseEquals(PUBLIC_MAPPED_GET_URL, resp);
		expectNullPhaseHeaders(resp);
		
		resp = sc2.doRequest(new HttpGet(PUBLIC_MAPPED_GET_URL));
		assertResponseEquals(PUBLIC_MAPPED_GET_URL, resp);
		expectNullPhaseHeaders(resp);
		// ---------------------------------------------------------------------

		// POST: PUBLIC POST ---------------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_POST_URL));
		assertResponseEquals(PUBLIC_POST_URL, resp);
		expectNullPhaseHeaders(resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_POST_URL));
		assertResponseEquals(PUBLIC_POST_URL, resp);
		expectNullPhaseHeaders(resp);
		// ---------------------------------------------------------------------

		// POST: PUBLIC MAPPED POST --------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_MAPPED_POST_URL));
		assertResponseEquals(PUBLIC_MAPPED_POST_URL, resp);
		expectNullPhaseHeaders(resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_MAPPED_POST_URL));
		assertResponseEquals(PUBLIC_MAPPED_POST_URL, resp);
		expectNullPhaseHeaders(resp);
		// ---------------------------------------------------------------------

		// GET: PUBLIC POST ----------------------------------------------------
		resp = sc1.doRequest(new HttpGet(PUBLIC_POST_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		
		resp = sc2.doRequest(new HttpGet(PUBLIC_POST_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		// ---------------------------------------------------------------------

		// GET: PUBLIC MAPPED POST ---------------------------------------------
		resp = sc1.doRequest(new HttpGet(PUBLIC_MAPPED_POST_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		
		resp = sc2.doRequest(new HttpGet(PUBLIC_MAPPED_POST_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		// ---------------------------------------------------------------------

		// POST: PUBLIC GET ----------------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_GET_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_GET_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		// ---------------------------------------------------------------------

		// POST: PUBLIC MAPPED GET ---------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_MAPPED_GET_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_MAPPED_GET_URL));
		assertStatus(HttpServletResponse.SC_NOT_FOUND, resp);
		// ---------------------------------------------------------------------
	}

	@Test
	public void testPublicRepeatble() {
		StringResponse resp;

		// Multiple @WebAction's...
		// GET -----------------------------------------------------------------
		resp = sc1.doRequest(new HttpGet(PUBLIC_WEBACTIONS_URL));
		assertResponseEquals(PUBLIC_WEBACTIONS_URL + "GET", resp);

		resp = sc2.doRequest(new HttpGet(PUBLIC_WEBACTIONS_URL));
		assertResponseEquals(PUBLIC_WEBACTIONS_URL + "GET", resp);
		// ---------------------------------------------------------------------
		
		// POST ----------------------------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_WEBACTIONS_URL));
		assertResponseEquals(PUBLIC_WEBACTIONS_URL + "POST", resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_WEBACTIONS_URL));
		assertResponseEquals(PUBLIC_WEBACTIONS_URL + "POST", resp);
		// ---------------------------------------------------------------------

		// Multiple methods, same @WebAction...
		// GET -----------------------------------------------------------------
		resp = sc1.doRequest(new HttpGet(PUBLIC_MULTIPLE_METHODS_URL));
		assertResponseEquals(PUBLIC_MULTIPLE_METHODS_URL + "GET", resp);
		
		resp = sc2.doRequest(new HttpGet(PUBLIC_MULTIPLE_METHODS_URL));
		assertResponseEquals(PUBLIC_MULTIPLE_METHODS_URL + "GET", resp);
		// ---------------------------------------------------------------------
		
		// POST ----------------------------------------------------------------
		resp = sc1.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_MULTIPLE_METHODS_URL));
		assertResponseEquals(PUBLIC_MULTIPLE_METHODS_URL + "POST", resp);
		
		resp = sc2.doRequest(new StringEntityPost("text/plain", "utf-8", PUBLIC_MULTIPLE_METHODS_URL));
		assertResponseEquals(PUBLIC_MULTIPLE_METHODS_URL + "POST", resp);
		// ---------------------------------------------------------------------
	}
	
	@Test
	public void testControllerRegisteredWithClassNames() {
		StringResponse resp;
		
		resp = sc2.doRequest(new HttpGet("/%s/get", Controller1.class.getSimpleName()));
		assertStatus(200, resp);
		
		resp = sc2.doRequest(new HttpGet("/%s/get", Controller2.class.getSimpleName()));
		assertStatus(200, resp);
	}
	// =========================================================================
}
