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

/**
 * User manager which handles CSRF security
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class CsrfUserManager implements UserManager {

	private final LazyInitializer<CsrfSecurityManager> csrfSecurityManager = new LazyInitializer() {

		@Override
		protected CsrfSecurityManager getLazyInstance(Object... params) {
			return CsrfUserManager.this._getCsrfSecurityManager();
		}
	};
	private final LazyInitializer<UserManager> userManager = new LazyInitializer() {
		@Override
		protected UserManager getLazyInstance(Object... params) {
			return CsrfUserManager.this._getUserManager();
		}
	};
	
	// CUSTOMIZABLE INITIALIZATION BEHAVIOUR -----------------------------------
	/**
	 * Returns the CSRF security manager used by this instance.
	 * This method is intended to be overridden to change object initialization and not be called directly
	 * @return the CSRF security manager used by this instance.
	 */
	protected abstract CsrfSecurityManager _getCsrfSecurityManager();
	
	public final CsrfSecurityManager getCsrfSecurityManager() {
		return csrfSecurityManager.getInstance();
	}
	
	/**
	 * Returns the User manager associated with this instance.
	 * @return the User manager associated with this instance
	 */
	protected abstract UserManager _getUserManager();
	
	public final UserManager getUserManager() {
		return userManager.getInstance();
	}
	// -------------------------------------------------------------------------
	
	@Override
	public final void login(HttpExchange exchange, User user) {
		getUserManager().login(exchange, user);
		getCsrfSecurityManager().registerToken(exchange);
	}

	@Override
	public final void logout(HttpExchange exchange) {
		getUserManager().logout(exchange);
		getCsrfSecurityManager().unregisterToken(exchange);
	}
	
	@Override
	public final User getUser(HttpExchange exchange) {
		return getUserManager().getUser(exchange);
	}
}
