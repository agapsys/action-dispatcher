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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserManager {
	// CLASS SCOPE =============================================================
	private static final String SESSION_ATTR_USER = "com.agapsys.web.user";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	/**
	 * Returns a user from session
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws IOException when there is an I/O error while processing the request
	 * @throws ServletException if the HTTP request cannot be handled
	 * @return session user or null if there is no user
	 */
	public User getSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		return (User) req.getSession().getAttribute(SESSION_ATTR_USER);
	}
	
	/**
	 * Sets user session
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws IOException when there is an I/O error while processing the request
	 * @throws ServletException if the HTTP request cannot be handled
	 * @param user user to be registered.
	 */
	public void setSessionUser(User user, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (user == null)
			throw new IllegalArgumentException("Null user");
		
		req.getSession().setAttribute(SESSION_ATTR_USER, user);
	}
	
	/**
	 * Clears session user
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws IOException when there is an I/O error while processing the request
	 * @throws ServletException if the HTTP request cannot be handled
	 */
	public void clearSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		req.getSession().removeAttribute(SESSION_ATTR_USER);
	}
	// =========================================================================
}
