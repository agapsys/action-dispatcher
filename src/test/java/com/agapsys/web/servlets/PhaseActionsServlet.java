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

package com.agapsys.web.servlets;

import com.agapsys.web.ActionServletTest;
import com.agapsys.web.HttpMethod;
import com.agapsys.web.annotations.AfterAction;
import com.agapsys.web.annotations.BeforeAction;
import com.agapsys.web.annotations.NotFoundAction;
import com.agapsys.web.annotations.WebAction;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/phase/*")
public class PhaseActionsServlet extends PublicServlet {
	
	@BeforeAction
	public void beforeAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		resp.setHeader(ActionServletTest.PHASE_BEFORE_HEADER, ActionServletTest.PHASE_BEFORE_HEADER);
	}
	
	@AfterAction
	public void afterAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		resp.setHeader(ActionServletTest.PHASE_AFTER_HEADER, ActionServletTest.PHASE_AFTER_HEADER);
	}
	
	@NotFoundAction
	public void onNotFound(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		resp.setHeader(ActionServletTest.PHASE_NOT_FOUND_HEADER, ActionServletTest.PHASE_NOT_FOUND_HEADER);
	}
	
	@Override
	@WebAction(httpMethod = HttpMethod.GET, defaultAction = true)
	public void get(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		processRequest(ActionServletTest.PHASE_DEFAULT_URL, req, resp);
	}
	
	@Override
	@WebAction(httpMethod = HttpMethod.POST, defaultAction = true)
	public void post(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		processRequest(ActionServletTest.PHASE_DEFAULT_URL, req, resp);
	}
}
