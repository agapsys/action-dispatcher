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
import java.util.Random;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public abstract class SecuredAction implements Action {
	// CLASS SCOPE =============================================================
	public static final String CSRF_HEADER  = "X-Csrf-Token";
	
	private static final String SESSION_ATTR_USER       = "com.agapsys.web.user";
	private static final String SESSION_ATTR_CSRF_TOKEN = "com.agapsys.web.csrf";
	
	private static final int CSRF_TOKEN_LENGTH = 128;
	
	/** 
	 * Generates a random string (chars: [a-z][A-Z][0-9]).
	 * @param length length of returned string
	 * @return a random string with given length.
	 * @throws IllegalArgumentException if (length &lt; 1)
	 */
	private static String getRandomString(int length) throws IllegalArgumentException {
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		return getRandomString(length, chars);
	}
	
	/**
	 * Generates a random String 
	 * @param length length of returned string
	 * @param chars set of chars which will be using during random string generation
	 * @return a random string with given length.
	 * @throws IllegalArgumentException if (length &lt; 1 || chars == null || chars.length == 0)
	 */
	private static String getRandomString(int length, char[] chars) throws IllegalArgumentException {
		if (length < 1)
			throw new IllegalArgumentException("Invalid length: " + length);
		
		if (chars == null || chars.length == 0)
			throw new IllegalArgumentException("Null/Empty chars");
		
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final Set<String> requiredRoles = new LinkedHashSet<>();
	
	/**
	 * Constructor.
	 * Creates an action with given required roles to be processed.
	 * @param requiredRoles required roles
	 */
	public SecuredAction(String...requiredRoles) {
		for (String role : requiredRoles) {
			if (!this.requiredRoles.add(role))
				throw new IllegalArgumentException("Duplicate role: " + role);
		}
	}
	
	/**
	 * Returns a user from session
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @return session user or null
	 */
	protected User getSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		return (User) req.getSession().getAttribute(SESSION_ATTR_USER);
	}
	
	/**
	 * Sets user session
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @param user user to be registered.
	 */
	protected void setSessionUser(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException, ServletException {
		if (user == null)
			throw new IllegalArgumentException("Null user");
		
		HttpSession session = req.getSession();
		session.setAttribute(SESSION_ATTR_USER, user);
		
		String csrfToken = getRandomString(CSRF_TOKEN_LENGTH);
		session.setAttribute(SESSION_ATTR_CSRF_TOKEN, csrfToken);
		resp.setHeader(CSRF_HEADER, csrfToken);
	}
	
	/**
	 * Returns a boolean indicating if given request is allowed to be processed.
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @return a boolean indicating if given request is allowed to be processed.
	 */
	protected boolean isAllowed(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (requiredRoles.isEmpty()) {
			return true;
		} else {
			User sessionUser = getSessionUser(req, resp);
			String sessionCsrfToken = (String) req.getSession().getAttribute(SESSION_ATTR_CSRF_TOKEN);
			String requestCsrfToken = req.getHeader(CSRF_HEADER);
			
			return (sessionUser != null && sessionUser.getRoles().containsAll(requiredRoles) && sessionCsrfToken.equals(requestCsrfToken));
		}
	}
	
	/**
	 * Called when given request is not allowed to be processed.
	 * @param req HTTP request
	 * @param resp HTTP response
	 */
	protected void onNotAllowed(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	}
	
	/**
	 * Actual action code.
	 * This method will be called only if given request is allowed to be processed.
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @see SecuredAction#isAllowed(HttpServletRequest, HttpServletResponse)
	 * @throws IOException when there is an error processing the request
	 * @throws ServletException when there is an error processing the request
	 */
	protected abstract void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;
	
	@Override
	public final void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (isAllowed(req, resp)) {
			onProcessRequest(req, resp);
		} else {
			onNotAllowed(req, resp);
		}
	}
	// =========================================================================
}
