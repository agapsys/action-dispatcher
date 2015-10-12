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
public class CsrfUserManager extends UserManager {
	// CLASS SCOPE =============================================================
	private static final CsrfSecurityManager DEFAULT_CSRF_SECURITY_MANAGER = new CsrfSecurityManager();
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final LazyInitializer<CsrfSecurityManager> csrfSecurityManager = new LazyInitializer() {

		@Override
		protected CsrfSecurityManager getLazyInstance(Object... params) {
			return CsrfUserManager.this._getCsrfSecurityManager();
		}
	};
	
	
	// CUSTOMIZABLE INITIALIZATION BEHAVIOUR -----------------------------------
	/**
	 * Returns the CSRF security manager used by this instance.
	 * This method is intended to be overridden to change object initialization and not be called directly
	 * @return the CSRF security manager used by this instance.
	 */
	protected CsrfSecurityManager _getCsrfSecurityManager() {
		return DEFAULT_CSRF_SECURITY_MANAGER;
	}
	// -------------------------------------------------------------------------
	
	// CUSTOMIZABLE RUNTIME BEHAVIOUR ------------------------------------------	
	@Override
	public void setSessionUser(HttpExchange exchange, SessionUser user) {
		super.setSessionUser(exchange, user);
		csrfSecurityManager.getInstance().generateSessionCsrfToken(exchange);
	}

	@Override
	public void clearSessionUser(HttpExchange exchange) {
		super.clearSessionUser(exchange);
		csrfSecurityManager.getInstance().clearSessionCsrfToken(exchange);
	}
	// -------------------------------------------------------------------------	
	// =========================================================================
}
