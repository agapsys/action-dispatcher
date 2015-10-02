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

package com.agapsys.web.action.dispatcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionCaller extends AbstractAction {
	private final Method method;
	private final ActionServlet servlet;
	
	public ActionCaller(ActionServlet servlet, Method method, SecurityHandler securityHandler) {
		super(securityHandler);
		
		if (servlet == null)
			throw new IllegalArgumentException("Null servlet");
		
		if (method == null)
			throw new IllegalArgumentException("Null method");
		
		this.servlet = servlet;
		this.method = method;
	}

	public final ActionServlet getServlet() {
		return servlet;
	}
	
	@Override
	protected void onNotAllowed(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		getServlet().onNotAllowed(req, resp);
	}

	@Override
	protected void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		try {
			method.invoke(getServlet(), req, resp);
		} catch (InvocationTargetException ex) {
			Throwable cause = ex.getCause();

			if (cause instanceof IOException)
				throw (IOException) cause;

			if (cause instanceof ServletException)
				throw (ServletException) cause;

			throw new RuntimeException(cause);
		} catch (IllegalAccessException | IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void sendError(HttpServletResponse resp, int status) throws IOException {
		getServlet().sendError(resp, status);
	}
}
