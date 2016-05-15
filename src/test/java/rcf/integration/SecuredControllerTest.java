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
package rcf.integration;

import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.HttpExchange;
import com.agapsys.rcf.User;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.sevlet.container.StacktraceErrorHandler;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcf.ServletContainerBuilder;

@WebController("secured")
public class SecuredControllerTest extends Controller {
	// STATIC CLASS ============================================================
	public static class AppUser implements User {
		
		public String[] roles;
		
		@Override
		public String[] getRoles() {
			return roles;
		}
		
		public AppUser(String...roles) {
			this.roles = roles;
		}
		
	}
	
	public static final String ROLE = "role";
	public static final String PARAM_ROLE = "role";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	public User user = null;
	private final Object mutex = new Object();
	
	@WebAction(secured = true)
	public void securedGet() {}
	
	@WebAction(requiredRoles = {ROLE})
	public void securedGetWithRoles() {}
	
	@WebAction
	public void logUser(HttpExchange exchange) {
		synchronized(mutex) {
			setUser(exchange, new AppUser(exchange.getOptionalRequestParameter(PARAM_ROLE, ""))); 
		}
	}
	
	@WebAction
	public void unlogUser(HttpExchange exchange) {
		synchronized(mutex) {
			setUser(exchange, null);
		}
	}

	@Override
	protected User getUser(HttpServletRequest req) {
		return user;
	}

	@Override
	protected void setUser(HttpExchange exchange, User user) {
		this.user = user;
	}

	// Test scope --------------------------------------------------------------
	ServletContainer sc;
	StringResponse resp;

	@Before
	public void before() {
		sc = new ServletContainerBuilder().registerController(SecuredControllerTest.class).setErrorHandler(new StacktraceErrorHandler()).build();
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
		resp = sc.doRequest(new HttpGet("/secured/logUser"));
		Assert.assertEquals(200, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGet"));
		Assert.assertEquals(200, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGetWithRoles"));
		Assert.assertEquals(403, resp.getStatusCode());
	}
	
	@Test
	public void testLoggedWithRoles() {
		resp = sc.doRequest(new HttpGet("/secured/logUser?%s=%s", PARAM_ROLE, ROLE));
		Assert.assertEquals(200, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGet"));
		Assert.assertEquals(200, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGetWithRoles"));
		Assert.assertEquals(200, resp.getStatusCode());
	}
	
	@Test
	public void testSignInAndSignOut() {
		
		// Log (with roles)
		resp = sc.doRequest(new HttpGet("/secured/logUser?%s=%s", PARAM_ROLE, ROLE));
		Assert.assertEquals(200, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGet"));
		Assert.assertEquals(200, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGetWithRoles"));
		Assert.assertEquals(200, resp.getStatusCode());
		
		// Unlogging
		resp = sc.doRequest(new HttpGet("/secured/unlogUser"));
		Assert.assertEquals(200, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGet"));
		Assert.assertEquals(401, resp.getStatusCode());
		resp = sc.doRequest(new HttpGet("/secured/securedGetWithRoles"));
		Assert.assertEquals(401, resp.getStatusCode());
	}
	// =========================================================================
}
