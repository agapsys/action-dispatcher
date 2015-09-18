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

package com.agapsys.web.actions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractAction implements Action {
	private final SecurityHandler securityHandler;
	
	/**
	 * Constructor.
	 * Creates an action with given security handler
	 * @param securityHandler security handler used by this action or null if there is no security
	 */
	public AbstractAction(SecurityHandler securityHandler) {
		this.securityHandler = securityHandler;
	}
	
	/**
	 * Constructor.
	 * Creates an action without any security handler
	 */
	public AbstractAction() {
		this(null);
	}
	
	/**
	 * Actual action code.
	 * This method will be called only if given request is allowed to be processed.
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @see SecurityHandler#isAllowed(HttpServletRequest, HttpServletResponse)
	 * @throws IOException when there is an error processing the request
	 * @throws ServletException when there is an error processing the request
	 */
	protected abstract void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;
	
	@Override
	public final void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (securityHandler != null) {
			if (securityHandler.isAllowed(req, resp)) {
				onProcessRequest(req, resp);
			} else {
				securityHandler.onNotAllowed(req, resp);
			}
		} else {
			onProcessRequest(req, resp);
		}
	}
}
