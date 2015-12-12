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
 * Represents a servlet security layer
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public interface ServletSecurityLayer {	
	/**
	 * Returns the security manager used when calling an {@linkplain Action action} with given set of required roles.
	 * @param requiredRoles action required roles
	 * @return the security manager used when calling an {@linkplain Action action} with given set of required roles.
	 */
	public SecurityManager getSecurityManager(Set<String> requiredRoles);
	
	/** 
	 * Returns the user manager used by the servlet using this security layer. 
	 * @return the user manager used by the servlet using this security layer.
	 */
	public UserManager getUserManager();
}
