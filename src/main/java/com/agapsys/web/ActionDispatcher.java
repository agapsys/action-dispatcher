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

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class ActionDispatcher {
	private final Map<HttpMethod, Map<String, WebAction>> ACTION_MAP = new LinkedHashMap<>();

	/**
	 * Registers an action with given URL
	 * @param action action to be associated with given URL and HTTP method
	 * @param httpMethod associated HTTP method
	 * @param url URL associated with given action and HTTP method
	 */
	public void registerAction(WebAction action, HttpMethod httpMethod, String url) {
		if (action == null) {
			throw new IllegalArgumentException("action == null");
		}

		if (httpMethod == null) {
			throw new IllegalArgumentException("httpMethod == null");
		}

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("url == null || url.trim().isEmpty()");
		}

		url = url.trim();

		Map<String, WebAction> map = ACTION_MAP.get(httpMethod);

		if (map == null) {
			map = new LinkedHashMap<>();
			ACTION_MAP.put(httpMethod, map);
		}

		if (map.containsKey(url)) {
			throw new IllegalArgumentException(String.format("Duplicate method/URL: %s/%s", httpMethod.name(), url));
		}

		map.put(url, action);
	}

	/**
	 * @return the action associated with given request and respective URL
	 * parameter map. If there is no mapping, returns null
	 * @param req HTTP request
	 */
	public WebAction getAction(HttpServletRequest req) {
		HttpMethod httpMethod;
		try {
			httpMethod = HttpMethod.valueOf(req.getMethod());
		} catch (IllegalArgumentException ex) {
			httpMethod = null;
		}
		
		String path = req.getPathInfo();

		Map<String, WebAction> map = ACTION_MAP.get(httpMethod);
		if (map == null) {
			return null;
		} else {
			return map.get(path);
		}
	}
}
