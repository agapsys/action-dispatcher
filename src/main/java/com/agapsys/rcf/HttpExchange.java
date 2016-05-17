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

import com.agapsys.rcf.exceptions.BadRequestException;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Default HttpExchange implementation. */
public class HttpExchange {
	// STATIC SCOPE ============================================================
	public static final HttpSerializer DEFAULT_SERIALIZER = new GsonHttpSerializer();
	public static final String SESSION_ATTR_USER = HttpExchange.class.getName() + ".sessionUser";

	/**
	 * @return origin IP
	 * @param req HTTP request
	 */
	public static String getOriginIp(HttpServletRequest req) {
		return req.getRemoteAddr();
	}

	/**
	 * @param req HTTP request
	 * @return origin user-agent
	 */
	public static String getOriginUserAgent(HttpServletRequest req) {
		return req.getHeader("user-agent");
	}

	/**
	 * @return request URI.
	 * @param req HTTP request
	 */
	public static String getRequestUri(HttpServletRequest req) {
		StringBuffer requestUrl = req.getRequestURL();
		if (req.getQueryString() != null) {
			requestUrl.append("?").append(req.getQueryString());
		}

		return String.format("%s %s %s", req.getMethod(), requestUrl.toString(), req.getProtocol());
	}

	/**
	 * @return cookie value. If there is no such cookie, returns null
	 * @param request HTTP request
	 * @param name cookie name
	 */
	public static String getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds a cookie.
	 * @param resp HTTP response
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
	 * @param path cookie path (usually {@linkplain HttpServletRequest#getContextPath()})
	 */
	public static void addCookie(HttpServletResponse resp, String name, String value, int maxAge, String path) {
		if (path == null || !path.startsWith("/"))
			throw new IllegalArgumentException("Invalid path: " + path);
		
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		resp.addCookie(cookie);
	}
	
	/**
	 * Adds a cookie for request context path
	 * @param req  HTTP request
	 * @param resp HTTP response
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
	 */
	public static void addCookie(HttpServletRequest req, HttpServletResponse resp, String name, String value, int maxAge) {
		addCookie(resp, name, value, maxAge, req.getContextPath());
	}
	
	/**
	 * Removes a cookie.
	 * @param resp HTTP response
	 * @param name name of the cookie to be removed
	 * @param path cookie path ((usually {@linkplain HttpServletRequest#getContextPath()})
	 */
	public static void removeCookie(HttpServletResponse resp, String name, String path) {
		addCookie(resp, name, null, 0, path);
	}
	
	/**
	 * Removes a cookie for request context path.
	 * @param req  HTTP request
	 * @param resp HTTP response
	 * @param name name of the cookie to be removed
	 */
	public static void removeCookie(HttpServletRequest req, HttpServletResponse resp, String name) {
		removeCookie(resp, name, req.getContextPath());
	}
	
	/**
	 * Returns an optional parameter contained in the request
	 * @param req  HTTP request
	 * @param paramName parameter name
	 * @param defaultValue default value if given parameter is not contained in the request
	 * @return parameter value
	 */
	public static String getOptionalParameter(HttpServletRequest req, String paramName, String defaultValue) {
		
		String val = req.getParameter(paramName);
		if (val == null || val.trim().isEmpty())
			val = defaultValue;
		
		val = val.trim();
		
		return val;
	}
	
	/**
	 * Returns a mandatory parameter contained in the request.
	 * @param req  HTTP request
	 * @param paramName parameter name
	 * @return parameter value.
	 * @throws BadRequestException if parameter is not contained in given request.
	 */
	public static String getMandatoryParameter(HttpServletRequest req, String paramName) throws BadRequestException {
		return getMandatoryParameter(req, paramName, "Missing parameter: %s", paramName);
	}
	
	/**
	 * Returns a mandatory parameter contained in the request.
	 * @param req  HTTP request
	 * @param paramName parameter name
	 * @param errorMessage error message used by thrown exception
	 * @param errMsgArgs optional error message args if error message is a formatted string.
	 * @return parameter value.
	 * @throws BadRequestException if parameter is not contained in given request.
	 */
	public static String getMandatoryParameter(HttpServletRequest req, String paramName, String errorMessage, Object...errMsgArgs) throws BadRequestException {
		String val = req.getParameter(paramName);
		if (val == null || val.trim().isEmpty()) {
			if (errMsgArgs.length > 0)
				errorMessage = String.format(errorMessage, errMsgArgs);
			
			throw new BadRequestException(errorMessage);
		}
		
		return val;
	}
	
	/**
	 * Reads an objected send with the request.
	 * @param <T> object type.
	 * @param req HTTP request.
	 * @param targetClass object class.
	 * @param serializer object serializer.
	 * @return read object.
	 * @throws BadRequestException if it was not possible to read an object of given type.
	 */
	public <T> T readObject(HttpSerializer serializer, HttpServletRequest req, Class<T> targetClass) throws BadRequestException, IOException {
		return serializer.readObject(req, targetClass);
	}
	
	/**
	 * Writes an object into response
	 * 
	 * @param serializer object serializer.
	 * @param resp HTTP response
	 * @param obj object to be written
	 * @throws IOException if an error happened during writing operation
	 */
	public void writeObject(HttpSerializer serializer, HttpServletResponse resp, Object obj) throws IOException {
		serializer.writeObject(resp, obj);
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final HttpServletRequest req;
	private final HttpServletResponse resp;
	private final LazyInitializer<HttpSerializer> serializer = new LazyInitializer<HttpSerializer>() {

		@Override
		protected HttpSerializer getLazyInstance() {
			return getObjectSerializer();
		}

	};
	
	/**
	 * Return the object serializer used by this instance. This method will be called only once.
	 * @return the object serializer used by this instance.
	 */
	protected HttpSerializer getObjectSerializer() {
		return DEFAULT_SERIALIZER;
	}
	

	public HttpExchange(HttpServletRequest req, HttpServletResponse resp) {
		
		if (req == null)
			throw new IllegalArgumentException("Request cannot be null");
		
		if (resp == null)
			throw new IllegalArgumentException("Response cannot be null");

		this.req = req;
		this.resp = resp;
	}

	public HttpServletRequest getRequest() {
		return req;
	}

	public HttpServletResponse getResponse() {
		return resp;
	}


	public String getRequestOriginIp() {
		return getOriginIp(getRequest());
	}

	public String getRequestUserAgent() {
		return getOriginUserAgent(getRequest());
	}

	public String getRequestUri() {
		return getRequestUri(getRequest());
	}

	public String getRequestCookieValue(String name) {
		return getCookieValue(getRequest(), name);
	}
	
	public void addCookie(String name, String value, int maxAge, String path) {
		addCookie(getResponse(), name, value, maxAge, path);
	}
	
	public void addCookie(String name, String value, int maxAge) {
		addCookie(getRequest(), getResponse(), name, value, maxAge);
	}

	public void removeCookie(String name, String path) {
		removeCookie(getResponse(), name, path);
	}
	
	public void removeCookie(String name) {
		removeCookie(getResponse(), name, getRequest().getContextPath());
	}
	
	public String getOptionalRequestParameter(String paramName, String defaultValue) {
		return getOptionalParameter(getRequest(), paramName, defaultValue);
	}
	
	public String getMandatoryRequestParameter(String paramName) throws BadRequestException {
		return getMandatoryParameter(getRequest(), paramName);
	}
	
	public String getMandatoryRequestParameter(String paramName, String errorMessage, Object...errMsgArgs) throws BadRequestException {
		return getMandatoryParameter(getRequest(), paramName, errorMessage, errMsgArgs);
	}	
	
	/**
	 * Returns the user associated with this HTTP exchange.
	 * @return the user associated with this HTTP exchange. Default implementation returns the user stored in session attribute {@linkplain HttpExchange#SESSION_ATTR_USER}.
	 */
	public User getCurrentUser() {
		return (User) getRequest().getSession().getAttribute(SESSION_ATTR_USER);
	}
	
	/**
	 * Sets the user associated with this HTTP exchange.
	 * @param user application user.
	 */
	public void setCurrentUser(User user) {
		getRequest().getSession().setAttribute(SESSION_ATTR_USER, user);
	}
	
	/**
	 * Reads an object sent with the request.
	 * @param <T> expected object type.
	 * @param targetClass expected object class.
	 * @return read object.
	 * @throws BadRequestException if it was not possible to read an object of given class.
	 */
	public <T> T readObject(Class<T> targetClass) throws BadRequestException, IOException {
		return readObject(serializer.getInstance(), getRequest(), targetClass);
	}
	
	/**
	 * Writes an object in the response.
	 * @param obj object to be written.
	 * @throws IOException If an error happens while sending the response.
	 */
	public void writeObject(Object obj) throws IOException {
		writeObject(serializer.getInstance(), getResponse(), obj);
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s", req.getMethod(), req.getRequestURI(), req.getProtocol());
	}
	// =========================================================================
}
