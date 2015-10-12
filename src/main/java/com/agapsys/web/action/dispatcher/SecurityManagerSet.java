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

import java.util.Set;

/**
 * Security manager which groups multiple security managers together.
 * If any of the managers reject the request, the request will be reject.
 * A request will be accepted only if ALL managers accept the request.
 * If there is no manager in the set, the request is allowed to be processed.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SecurityManagerSet implements SecurityManager {
	private final Set<SecurityManager> securityManagerSet;
	
	/**
	 * Constructor
	 * @param securityManagerSet set of security managers
	 */
	public SecurityManagerSet(Set<SecurityManager> securityManagerSet) {
		this.securityManagerSet = securityManagerSet;
	}

	/**
	 * Return the set of security managers passed in constructor.
	 * @return the set of security managers passed in constructor.
	 */
	protected Set<SecurityManager> getSecurityManagerSet() {
		return securityManagerSet;
	}
	
	@Override
	public boolean isAllowed(HttpExchange exchange) {
		if (securityManagerSet == null)
			return true;
		
		for (SecurityManager securityManager : securityManagerSet) {
			if (!securityManager.isAllowed(exchange))
				return false;
		}
		
		return true;
	}
}
