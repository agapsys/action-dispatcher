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

import java.util.Objects;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Security handler responsible by checking for CSRF attacks
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class CsrfSecurityHandler implements SecurityHandler {
	// CLASS SCOPE =============================================================
	private static final String SESSION_ATTR_CSRF_TOKEN = "com.agapsys.web.csrfToken";
	private static final int    CSRF_TOKEN_LENGTH       = 128;
	
	/** Name of the header used to send/retrieve a CSRF token. */
	public static final String CSRF_HEADER  = "X-Csrf-Token";

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
	/** 
	 * Generates a CSRF token
	 * @return generated CSRF token
	 */
	public String generateCsrfToken() {
		return getRandomString(CSRF_TOKEN_LENGTH);
	}
	
	/**
	 * Returns the CSRF token stored in session
	 * @param exchange HTTP exchange
	 * @return the CSRF token stored in session
	 */
	public String getSessionCsrfToken(HttpExchange exchange) {
		return (String) exchange.getRequest().getSession().getAttribute(SESSION_ATTR_CSRF_TOKEN);
	}
	
	/**
	 * Stores a CSRF token in the session
	 * @param csrfToken token to be stored
	 * @param exchange HTTP exchange
	 */
	public void setSessionCsrfToken(HttpExchange exchange, String csrfToken) {	
		if (csrfToken == null || csrfToken.isEmpty())
			throw new IllegalArgumentException("Null/Empty CSRF token");
		
		exchange.getRequest().getSession().setAttribute(SESSION_ATTR_CSRF_TOKEN, csrfToken);
	}
	
	/** 
	 * Clears session CSRF token
	 * @param exchange HTTP exchange
	 */
	public void clearCsrfToken(HttpExchange exchange) {
		exchange.getRequest().getSession().removeAttribute(SESSION_ATTR_CSRF_TOKEN);
	}
	
	/**
	 * Convenience method for sendCsrfToken(csrfToken, he, false)
	 * @param csrfToken token to be sent
	 * @param exchange HTTP exchange
	 */
	public void sendCsrfToken(HttpExchange exchange, String csrfToken) {
		if (csrfToken == null || csrfToken.isEmpty())
			throw new IllegalArgumentException("Null/Empty CSRF token");
		
		HttpServletResponse resp = exchange.getResponse();
		resp.setHeader(CSRF_HEADER, csrfToken);
	}
	
	@Override
	public boolean isAllowed(HttpExchange exchange) {
		HttpServletRequest req = exchange.getRequest();
		
		String sessionCsrfToken = (String) req.getSession().getAttribute(SESSION_ATTR_CSRF_TOKEN);
		String requestCsrfToken = req.getHeader(CSRF_HEADER);
			
		return Objects.equals(sessionCsrfToken, requestCsrfToken);
	}
	// =========================================================================
}
