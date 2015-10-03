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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Security handler which groups multiple security handlers together.
 * If any of the handlers reject the request, the request will be reject.
 * A request will be accepted only if ALL handlers accept the request of if there is no handler in associated set of handlers.
 * If there is no handler in the set, the request is allowed to be processed.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SecurityHandlerSet implements SecurityHandler {
	private final Set<SecurityHandler> handlerSet;
	
	/**
	 * Constructor
	 * @param handlerSet set of security handlers
	 */
	public SecurityHandlerSet(Set<SecurityHandler> handlerSet) {
		this.handlerSet = handlerSet;
	}

	/**
	 * Return the set of security handlers passed in constructor.
	 * @return the set of security handlers passed in constructor.
	 */
	protected Set<SecurityHandler> getSecuredHandlerSet() {
		return handlerSet;
	}
	
	@Override
	public boolean isAllowed(HttpServletRequest req, HttpServletResponse resp) {
		if (handlerSet == null)
			return true;
		
		for (SecurityHandler handler : handlerSet) {
			if (!handler.isAllowed(req, resp))
				return false;
		}
		
		return true;
	}
}
