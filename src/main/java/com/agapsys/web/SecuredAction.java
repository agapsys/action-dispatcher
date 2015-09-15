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

package com.agapsys.web;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SecuredAction implements Action {
	// CLASS SCOPE =============================================================
	private static final String SESSION_ATTR_USER       = "com.agapsys.web.user";
	private static final String SESSION_ATTR_CSRF_TOKEN = "com.agapsys.web.csrf";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final Set<String> requiredRoles = new LinkedHashSet<>();
	
	public SecuredAction(String...requiredRoles) {
		for (String role : requiredRoles) {
			if (!this.requiredRoles.add(role))
				throw new IllegalArgumentException("Duplicate definition of required role: " + role);
		}
	}
	
	protected User getSessionUser(HttpServletRequest req) {
		return (User) req.getAttribute(SESSION_ATTR_USER);
	}
	
	protected void registerSessionUser(HttpServletRequest req, User user) {
		
	}
			
	protected boolean isAllowed(HttpServletRequest req) {
		//TODO
		return false;
	}
	
	protected void onNotAllowd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	}
	
	protected abstract void onProcessRequest(HttpServletRequest req, HttpServletResponse resp);
	
	@Override
	public final void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (isAllowed(req)) {
			onProcessRequest(req, resp);
		} else {
			onNotAllowd(req, resp);
		}
	}
	// =========================================================================
}
