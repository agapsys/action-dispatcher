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

package action.dispatcher.integration.servlets;

import com.agapsys.web.action.dispatcher.ActionServlet;
import com.agapsys.web.action.dispatcher.SessionUser;
import com.agapsys.web.action.dispatcher.HttpExchange;
import com.agapsys.web.action.dispatcher.WebAction;
import action.dispatcher.integration.ActionServletGeneralTest;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;

@WebServlet("/login/*")
public class LoginServlet extends ActionServlet {
	// CLASS SCOPE =============================================================
	private static class SimpleUser implements SessionUser {
		private final Set<String> roles = new LinkedHashSet<>();
		
		@Override
		public Set<String> getRoles() {
			return roles;
		}
	}
	private static class PriviledgedUser extends SimpleUser {
		public PriviledgedUser() {
			super.getRoles().add(SecuredServlet.SECURED_ROLE);
		}
	}
	
	private final SessionUser simpleUser      = new SimpleUser();
	private final SessionUser priviledgedUser = new PriviledgedUser();
	
	private void sendMessage(String msg, HttpExchange exchange) {
		try {
			exchange.getResponse().getWriter().print(msg);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@WebAction
	public void simple(HttpExchange exchange) {
		getUserManager().setSessionUser(exchange, simpleUser);
		sendMessage(ActionServletGeneralTest.LOGIN_SIMPLE_USER_URL, exchange);
	}
	
	@WebAction
	public void priviledged(HttpExchange exchange) {
		getUserManager().setSessionUser(exchange, priviledgedUser);
		sendMessage(ActionServletGeneralTest.LOGIN_PRIVILDGED_USER_URL, exchange);
	}
}
