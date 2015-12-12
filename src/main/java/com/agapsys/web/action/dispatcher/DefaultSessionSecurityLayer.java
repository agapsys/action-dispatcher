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

import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultSessionSecurityLayer implements ServletSecurityLayer {
	// CLASS SCOPE =============================================================
	private static final UserManager DEFAULT_USER_MANAGER = new SessionCsrfUserManager();
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final LazyInitializer<UserManager> userManager = new LazyInitializer() {

		@Override
		protected Object getLazyInstance(Object... params) {
			return _getUserManager();
		}
	};
	
	@Override
	public SecurityManager getSecurityManager(Set<String> requiredRoles) {
		if (requiredRoles == null || requiredRoles.isEmpty()) { // Ignores CSRF token if there is no required roles
			return null;
		} else {
			Set<SecurityManager> securityManagerSet = new LinkedHashSet<>();
			securityManagerSet.add(((CsrfUserManager)getUserManager()).getCsrfSecurityManager());

			final UserRoleSecurityManager userRoleSecurityManager = new UserRoleSecurityManager(getUserManager(), requiredRoles);
			securityManagerSet.add(userRoleSecurityManager);

			return new SecurityManagerSet(securityManagerSet);
		}
	}

	protected UserManager _getUserManager() {
		return DEFAULT_USER_MANAGER;
	}
	
	@Override
	public final UserManager getUserManager() {
		return userManager.getInstance();
	}
	// =========================================================================
}
