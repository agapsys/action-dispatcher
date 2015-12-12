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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Default HttpExchange implementation. */
public class DefaultHttpExchange implements HttpExchange {
	private final ActionServlet servlet;
	private final HttpServletRequest req;
	private final HttpServletResponse resp;

	public DefaultHttpExchange(ActionServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
		if (servlet == null)
			throw new IllegalArgumentException("Servlet cannot be null");
		
		if (req == null)
			throw new IllegalArgumentException("Request cannot be null");
		
		if (resp == null)
			throw new IllegalArgumentException("Response cannot be null");
		this.servlet = servlet;
		this.req = req;
		this.resp = resp;
	}

	public final ActionServlet getServlet() {
		return servlet;
	}

	@Override
	public final HttpServletRequest getRequest() {
		return req;
	}

	@Override
	public final HttpServletResponse getResponse() {
		return resp;
	}

	@Override
	public SessionUser getSessionUser() {
		return getServlet().getUserManager().getSessionUser(this);
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", req.getMethod(), req.getRequestURI(), req.getProtocol());
	}
}
