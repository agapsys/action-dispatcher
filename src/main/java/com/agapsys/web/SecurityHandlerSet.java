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

import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SecurityHandlerSet extends AbstractSecurityHandler {
	private final Set<SecurityHandler> handlerSet;
	
	public SecurityHandlerSet(Set<SecurityHandler> handlerSet) {
		this.handlerSet = handlerSet;
	}

	@Override
	public boolean isAllowed(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (handlerSet == null)
			return true;
		
		for (SecurityHandler handler : handlerSet) {
			if (!handler.isAllowed(req, resp))
				return false;
		}
		
		return true;
	}
}
