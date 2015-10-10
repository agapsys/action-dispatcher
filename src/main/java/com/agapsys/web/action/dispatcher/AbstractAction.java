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
	 * Called before action processing.
	 * This method will be called only if action is allowed to be processed. Default implementation does nothing.
	 * @param exchange HTTP exchange
	 */
	protected void beforeAction(HttpExchange exchange) {}
	
	/**
	 * Actual action code.
	 * This method will be called only if given request is allowed to be processed.
	 * @param exchange HTTP exchange
	 * @see SecurityHandler#isAllowed(HttpExchange)
	 */
	protected abstract void onProcessRequest(HttpExchange exchange);
	
	/** 
	 * Called after action processing.
	 * This method will be called only if action is allowed to be processed and the action was processed successfully. Default implementation does nothing.
	 * @param exchange HTTP exchange
	 */
	protected void afterAction(HttpExchange exchange) {}
	
	/**
	 * Called when given request is not allowed to be processed.
	 * Default implementation just set a {@linkplain HttpServletResponse#SC_FORBIDDEN} status in the response
	 * @param exchange HTTP exchange
	 */
	protected void onNotAllowed(HttpExchange exchange) {
		exchange.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	}
	
	@Override
	public final void processRequest(HttpExchange exchange) {
		if (securityHandler != null) {
			if (securityHandler.isAllowed(exchange)) {
				beforeAction(exchange);
				onProcessRequest(exchange);
				afterAction(exchange);
			} else {
				onNotAllowed(exchange);
			}
		} else {
			beforeAction(exchange);
			onProcessRequest(exchange);
			afterAction(exchange);
		}
	}
}
