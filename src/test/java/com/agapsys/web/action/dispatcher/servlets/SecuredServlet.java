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
import com.agapsys.web.action.dispatcher.ActionServletGeneralTest;
import com.agapsys.web.action.dispatcher.HttpMethod;
import com.agapsys.web.action.dispatcher.WebAction;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/secured/*")
public class SecuredServlet extends ActionServlet {
	public static final String SECURED_ROLE = "secured";
	// CLASS SCOPE =============================================================

	// =========================================================================
	private void processAction(String msg, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().print(msg);
	}
	
	// INSTANCE SCOPE ==========================================================
	@WebAction(requiredRoles = {SECURED_ROLE})
	public void get(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processAction(ActionServletGeneralTest.SECURED_GET_URL, req, resp);
	}
	
	@WebAction(mapping = "mapped/get", requiredRoles = {SECURED_ROLE})
	public void mappedGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processAction(ActionServletGeneralTest.SECURED_MAPPED_GET_URL, req, resp);
	}
	
	
	@WebAction(httpMethod = HttpMethod.POST, requiredRoles = {SECURED_ROLE})
	public void post(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processAction(ActionServletGeneralTest.SECURED_POST_URL, req, resp);
	}
	
	@WebAction(httpMethod = HttpMethod.POST, mapping = "mapped/post", requiredRoles = {SECURED_ROLE})
	public void mappedPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processAction(ActionServletGeneralTest.SECURED_MAPPED_POST_URL, req, resp);
	}
	// =========================================================================
}
