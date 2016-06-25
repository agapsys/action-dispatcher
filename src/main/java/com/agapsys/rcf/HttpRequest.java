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

import com.agapsys.rcf.exceptions.BadRequestException;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class HttpRequest {
	private final HttpServletRequest coreRequest;
	public HttpServletRequest getCoreRequest() {
		return coreRequest;
	}

	private final HttpExchange exchange;
	public HttpExchange getExchange() {
		return exchange;
	}

	public HttpRequest(HttpExchange exchange, HttpServletRequest coreRequest) {
		if (exchange == null) throw new IllegalArgumentException("Exchange cannot be null");
		if (coreRequest == null) throw new IllegalArgumentException("Core request cannot be null");

		this.coreRequest = coreRequest;
		this.exchange = exchange;
	}

	/**
	 * Return origin IP.
	 *
	 * @return origin IP.
	 */
	public String getOriginIp() {
		return getCoreRequest().getRemoteAddr();
	}

	/**
	 * Return origin user-agent.
	 *
	 * @return origin user-agent.
	 */
	public String getUserAgent() {
		return getCoreRequest().getHeader("user-agent");
	}

	/**
	 * Return request URI.
	 *
	 * @return request URI.
	 */
	public String getUri() {
		HttpServletRequest req = getCoreRequest();

		StringBuffer requestUrl = req.getRequestURL();
		if (req.getQueryString() != null) {
			requestUrl.append("?").append(req.getQueryString());
		}

		return String.format("%s %s %s", req.getMethod(), requestUrl.toString(), req.getProtocol());
	}

	/**
	 * Return cookie value.
	 *
	 * @return cookie value. If there is no such cookie, returns null.
	 * @param name cookie name
	 */
	public String getCookieValue(String name) {
		Cookie[] cookies = getCoreRequest().getCookies();
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
	 * Returns an optional parameter contained in the request.
	 *
	 * @param paramName parameter name
	 * @param defaultValue default value if given parameter is not contained in the request
	 * @return parameter value
	 */
	public String getOptionalParameter(String paramName, String defaultValue) {

		String val = getCoreRequest().getParameter(paramName);
		if (val == null || val.trim().isEmpty())
			val = defaultValue;

		if (val != null)
			val = val.trim();

		return val;
	}

	/**
	 * Returns a mandatory parameter contained in the request.
	 *
	 * @param paramName parameter name
	 * @return parameter value.
	 * @throws BadRequestException if parameter is not contained in given request.
	 */
	public String getMandatoryParameter(String paramName) throws BadRequestException {
		return getMandatoryParameter(paramName, "Missing parameter: %s", paramName);
	}

	/**
	 * Returns a mandatory parameter contained in the request.
	 *
	 * @param paramName parameter name
	 * @param errorMessage error message if parameter is not found.
	 * @param errMsgArgs optional error message args if error message is a formatted string.
	 * @return parameter value.
	 * @throws BadRequestException if parameter is not contained in given request.
	 */
	public String getMandatoryParameter(String paramName, String errorMessage, Object...errMsgArgs) throws BadRequestException {
		String val = getCoreRequest().getParameter(paramName);

		if (val == null || val.trim().isEmpty()) {
			if (errMsgArgs.length > 0)
				errorMessage = String.format(errorMessage, errMsgArgs);

			throw new BadRequestException(errorMessage);
		}

		return val;
	}

	/**
	 * Reads an objected contained in the request.
	 *
	 * @param <T> object type.
	 * @param targetClass object class.
	 * @param serializer serializer used to get the object.
	 * @return read object.
	 * @throws BadRequestException if it was not possible to read an object of given type.
	 * @throws IOException if there was an error while reading the object.
	 */
	public <T> T readObject(HttpObjectSerializer serializer, Class<T> targetClass) throws BadRequestException, IOException {
		return serializer.readObject(getCoreRequest(), targetClass);
	}

	/**
	 * Reads an objected send with the request using exchange default serializer.
	 *
	 * @param <T> object type.
	 * @param targetClass object class.
	 * @return read object.
	 * @throws BadRequestException if it was not possible to read an object of given type.
	 * @throws IOException if there was an error while reading the object.
	 */
	public final <T> T readObject(Class<T> targetClass) throws BadRequestException, IOException {
		return readObject(getExchange().getHttpObjectSerializer(), targetClass);
	}
}
