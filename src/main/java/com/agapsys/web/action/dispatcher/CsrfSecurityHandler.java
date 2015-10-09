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
	 * @param rrp request-response pair
	 * @return the CSRF token stored in session
	 */
	public String getSessionCsrfToken(RequestResponsePair rrp) {
		return (String) rrp.getRequest().getSession().getAttribute(SESSION_ATTR_CSRF_TOKEN);
	}
	
	/**
	 * Stores a CSRF token in the session
	 * @param csrfToken token to be stored
	 * @param rrp request-response pair
	 */
	public void setSessionCsrfToken(String csrfToken, RequestResponsePair rrp) {	
		if (csrfToken == null || csrfToken.isEmpty())
			throw new IllegalArgumentException("Null/Empty CSRF token");
		
		rrp.getRequest().getSession().setAttribute(SESSION_ATTR_CSRF_TOKEN, csrfToken);
	}
	
	/** 
	 * Clears session CSRF token
	 * @param rrp request-response pair
	 */
	public void clearCsrfToken(RequestResponsePair rrp) {
		rrp.getRequest().getSession().removeAttribute(SESSION_ATTR_CSRF_TOKEN);
	}
	
	/**
	 * Sends a CSRF token to the client.
	 * Default implementation sends a {@linkplain CsrfSecurityHandler#CSRF_HEADER header} with given token.
	 * @param csrfToken token to be sent
	 * @param rrp request-response pair
	 * @param flush defines if response shall be flushed
	 */
	public void sendCsrfToken(String csrfToken, RequestResponsePair rrp, boolean flush) {
		if (csrfToken == null || csrfToken.isEmpty())
			throw new IllegalArgumentException("Null/Empty CSRF token");
		
		HttpServletResponse resp = rrp.getResponse();
		
		resp.setHeader(CSRF_HEADER, csrfToken);
		
		if (flush) {
			try {
				resp.flushBuffer();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	/**
	 * Convenience method for sendCsrfToken(csrfToken, rrp, false)
	 * @param csrfToken token to be sent
	 * @param rrp request-response pair
	 */
	public void sendCsrfToken(String csrfToken, RequestResponsePair rrp) {
		sendCsrfToken(csrfToken, rrp, false);
	}
	
	@Override
	public boolean isAllowed(RequestResponsePair rrp) {
		HttpServletRequest req = rrp.getRequest();
		
		String sessionCsrfToken = (String) req.getSession().getAttribute(SESSION_ATTR_CSRF_TOKEN);
		String requestCsrfToken = req.getHeader(CSRF_HEADER);
			
		return Objects.equals(sessionCsrfToken, requestCsrfToken);
	}
	// =========================================================================
}
