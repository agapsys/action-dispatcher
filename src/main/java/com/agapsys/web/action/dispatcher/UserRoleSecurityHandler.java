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
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserRoleSecurityHandler extends AbstractSecurityHandler {
	
	
	// INSTANCE SCOPE ==========================================================
	private final Set<String> requiredRoles;
	private final UserManager userManager;
	
	/**
	 * Constructor.
	 * Creates a security handler with given required roles
	 * @param requiredRoles required roles. Passing null or an empty set implies in no security
	 */
	public UserRoleSecurityHandler(UserManager userManager, Set<String> requiredRoles) {
		this.requiredRoles = requiredRoles;
		this.userManager = userManager;
	}
		
	@Override
	public boolean isAllowed(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (requiredRoles == null || requiredRoles.isEmpty()) {
			return true;
		} else {
			User sessionUser = userManager != null ? userManager.getSessionUser(req, resp) : null;
			return sessionUser != null && sessionUser.getRoles().containsAll(requiredRoles);
		}
	}
	
	@Override
	public void onNotAllowed(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		User sessionUser = userManager != null ? userManager.getSessionUser(req, resp) : null;
		if (sessionUser == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			super.onNotAllowed(req, resp);
		}
	}
	
	
	// =========================================================================

	
}
