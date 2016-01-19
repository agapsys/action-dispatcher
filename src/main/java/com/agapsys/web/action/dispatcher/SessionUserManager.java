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

import javax.servlet.http.HttpSession;

/**
 * Session-based user manager.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SessionUserManager implements UserManager {
	// CLASS SCOPE =============================================================
	private static final String SESSION_ATTR_USER = "com.agapsys.web.action.dispatcher.user";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	public User getUser(HttpExchange exchange) {
		HttpSession session = exchange.getRequest().getSession(false);
		if (session == null)
			return null;
		
		return (User) session.getAttribute(SESSION_ATTR_USER);
	}
	
	@Override
	public void login(HttpExchange exchange, User user) {
		if (user == null)
			throw new IllegalArgumentException("Null user");
		
		exchange.getRequest().getSession().setAttribute(SESSION_ATTR_USER, user);
	}
	
	@Override
	public void logout(HttpExchange exchange) {
		HttpSession session = exchange.getRequest().getSession(false);
		if (session != null)
			session.removeAttribute(SESSION_ATTR_USER);
	}
	// =========================================================================
}
