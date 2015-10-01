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

package com.agapsys.web.action.dispatcher.servlets;

import com.agapsys.web.action.dispatcher.ActionServlet;
import com.agapsys.web.action.dispatcher.ActionServletTest;
import com.agapsys.web.action.dispatcher.ApplicationUser;
import com.agapsys.web.action.dispatcher.WebAction;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login/*")
public class LoginServlet extends ActionServlet {
	// CLASS SCOPE =============================================================
	private static class SimpleUser implements ApplicationUser {
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
	
	private final ApplicationUser simpleUser      = new SimpleUser();
	private final ApplicationUser priviledgedUser = new PriviledgedUser();
	
	private void sendMessage(String msg, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().print(msg);
	}
	
	@WebAction
	public void simple(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		getUserManager().setSessionUser(simpleUser, req, resp);
		sendMessage(ActionServletTest.LOGIN_SIMPLE_USER_URL, req, resp);
	}
	
	@WebAction
	public void priviledged(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		getUserManager().setSessionUser(priviledgedUser, req, resp);
		sendMessage(ActionServletTest.LOGIN_PRIVILDGED_USER_URL, req, resp);
	}
}
