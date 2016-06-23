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

package com.agapsys.rcf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Default HttpExchange implementation. */
public class HttpExchange {
	// STATIC SCOPE ============================================================
	public static final HttpObjectSerializer DEFAULT_SERIALIZER = new GsonHttpSerializer();
	public static final String SESSION_ATTR_USER = HttpExchange.class.getName() + ".sessionUser";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final HttpServletRequest coreRequest;
	public HttpServletRequest getCoreRequest() {
		return coreRequest;
	}

	private final HttpServletResponse coreResponse;
	public HttpServletResponse getCoreResponse() {
		return coreResponse;
	}

	public HttpExchange(HttpServletRequest coreRequest, HttpServletResponse coreResponse) {

		if (coreRequest == null)
			throw new IllegalArgumentException("Core request cannot be null");

		if (coreResponse == null)
			throw new IllegalArgumentException("Core response cannot be null");

		this.coreRequest = coreRequest;
		this.coreResponse = coreResponse;
	}

	// -------------------------------------------------------------------------
	private HttpObjectSerializer serializer = null;

	/**
	 * Returns the default serializer used by this exchange.
	 *
	 * @return the default serializer used by this exchange.
	 */
	public final HttpObjectSerializer getHttpObjectSerializer() {
		if (serializer == null) {
			serializer = getCustomHttpObjectSerializer();
		}
		return serializer;
	}

	/**
	 * Returns a customized serializer used by this exchange.
	 *
	 * @return a customized serializer used by this exchange.
	 */
	protected HttpObjectSerializer getCustomHttpObjectSerializer() {
		return DEFAULT_SERIALIZER;
	}
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	private HttpRequest req;
	/**
	 * Returns the request associated with this exchange.
	 *
	 * @return the request associated with this exchange.
	 */
	public final HttpRequest getRequest() {
		if (req == null) {
			req = getCustomRequest(getCoreRequest());
		}

		return req;
	}

	/**
	 * Returns a customized instance of request.
	 *
	 * @param coreRequest core HTTP request.
	 * @return customized request.
	 */
	protected HttpRequest getCustomRequest(HttpServletRequest coreRequest) {
		return new HttpRequest(this, getCoreRequest());
	}
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	private HttpResponse resp;

	/**
	 * Returns the response associated with this exchange.
	 *
	 * @return the response associated with this exchange.
	 */
	public final HttpResponse getResponse() {
		if (resp == null) {
			resp = getCustomResponse(getCoreResponse());
		}

		return resp;
	}

	/**
	 * Returns a customized instance of response.
	 *
	 * @param coreResponse core HTTP response.
	 * @return customized response.
	 */
	protected HttpResponse getCustomResponse(HttpServletResponse coreResponse) {
		return new HttpResponse(this, getCoreResponse());
	}
	// -------------------------------------------------------------------------


	/**
	 * Returns the user associated with this HTTP exchange.
	 *
	 * @return the user associated with this HTTP exchange. Default implementation returns the user stored in session attribute {@linkplain HttpExchange#SESSION_ATTR_USER}.
	 */
	public User getCurrentUser() {
		return (User) getCoreRequest().getSession().getAttribute(SESSION_ATTR_USER);
	}

	/**
	 * Sets the user associated with this HTTP exchange.
	 * @param user application user.
	 */
	public void setCurrentUser(User user) {
		getCoreRequest().getSession().setAttribute(SESSION_ATTR_USER, user);
	}


	@Override
	public String toString() {
		return String.format("%s %s %s", getCoreRequest().getMethod(), getCoreRequest().getRequestURI(), getCoreRequest().getProtocol());
	}
	// =========================================================================
}
