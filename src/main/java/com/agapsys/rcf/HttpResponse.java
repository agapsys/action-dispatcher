/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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

import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class HttpResponse {

	private final HttpServletResponse coreResponse;
	public HttpServletResponse getCoreResponse() {
		return coreResponse;
	}

	private final HttpExchange exchange;
	public HttpExchange getExchange() {
		return exchange;
	}

	public HttpResponse(HttpExchange exchange, HttpServletResponse coreResponse) {
		if (exchange == null) throw new IllegalArgumentException("Exchange cannot be null");
		if (coreResponse == null) throw new IllegalArgumentException("Core response cannot be null");

		this.coreResponse = coreResponse;
		this.exchange = exchange;
	}

	/**
	 * Adds a cookie.
	 *
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
	 * @param path cookie path (usually {@linkplain HttpServletRequest#getContextPath()})
	 */
	public void addCookie(String name, String value, int maxAge, String path) {
		if (path == null || !path.startsWith("/"))
			throw new IllegalArgumentException("Invalid path: " + path);

		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		getCoreResponse().addCookie(cookie);
	}

	/**
	 * Adds a cookie for request context path.
	 *
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
	 */
	public final void addCookie(String name, String value, int maxAge) {
		addCookie(name, value, maxAge, getExchange().getRequest().getCoreRequest().getContextPath());
	}

	/**
	 * Removes a cookie.
	 *
	 * @param name name of the cookie to be removed
	 * @param path cookie path ((usually {@linkplain HttpServletRequest#getContextPath()})
	 */
	public void removeCookie(String name, String path) {
		addCookie(name, null, 0, path);
	}

	/**
	 * Removes a cookie for request context path.
	 *
	 * @param name name of the cookie to be removed
	 */
	public final void removeCookie(String name) {
		removeCookie(name, getExchange().getRequest().getCoreRequest().getContextPath());
	}

	/**
	 * Writes an object into response
	 *
	 * @param serializer object serializer.
	 * @param obj object to be written
	 * @throws IOException if an error happened during writing operation
	 */
	public void writeObject(HttpObjectSerializer serializer, Object obj) throws IOException {
		serializer.writeObject(getCoreResponse(), obj);
	}

	/**
	 * Writes an object into response using exchange default serializer.
	 *
	 * @param obj object to be written
	 * @throws IOException if an error happened during writing operation
	 */
	public final void writeObject(Object obj) throws IOException {
		writeObject(getExchange().getHttpObjectSerializer(), obj);
	}
}
