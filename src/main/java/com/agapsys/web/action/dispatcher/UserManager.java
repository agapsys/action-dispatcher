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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User manager.
 * A user manager is the object responsible by handling user in a request session
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class UserManager {
	// CLASS SCOPE =============================================================
	private static final String SESSION_ATTR_USER = "com.agapsys.web.user";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	/**
	 * Returns a user from session
	 * @param req HTTP request
	 * @return session user or null if there is no user
	 */
	public ApplicationUser getSessionUser(HttpServletRequest req) {
		return (ApplicationUser) req.getSession().getAttribute(SESSION_ATTR_USER);
	}
	
	/**
	 * Sets a user in a session
	 * @param req HTTP request
	 * @param user user to be registered.
	 * @param resp HTTP response (used when there is a need to send data to user after setting the user in request session)
	 * @throws IOException when there is an I/O error while send the response.
	 */
	public void setSessionUser(ApplicationUser user, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (user == null)
			throw new IllegalArgumentException("Null user");
		
		req.getSession().setAttribute(SESSION_ATTR_USER, user);
	}
	
	/**
	 * Clears session user
	 * @param req HTTP request
	 */
	public void clearSessionUser(HttpServletRequest req) {
		req.getSession().removeAttribute(SESSION_ATTR_USER);
	}
	// =========================================================================
}
