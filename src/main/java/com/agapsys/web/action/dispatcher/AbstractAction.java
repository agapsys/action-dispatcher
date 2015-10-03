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
	 * Called before action processing.
	 * This method will be called only if action is allowed to be processed. Default implementation does nothing.
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws IOException if an input or output error occurs while handling the HTTP request
	 * @throws ServletException if the HTTP request cannot be handled
	 */
	protected void beforeAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {}
	
	/**
	 * Actual action code.
	 * This method will be called only if given request is allowed to be processed.
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @see SecurityHandler#isAllowed(HttpServletRequest, HttpServletResponse)
	 * @throws IOException if an input or output error occurs while handling the HTTP request
	 * @throws ServletException if the HTTP request cannot be handled
	 */
	protected abstract void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;
	
	/** 
	 * Called after action processing.
	 * This method will be called only if action is allowed to be processed and the action was processed successfully. Default implementation does nothing.
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws IOException if an input or output error occurs while handling the HTTP request
	 * @throws ServletException if the HTTP request cannot be handled
	 */
	protected void afterAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {}
	
	/**
	 * Called when given request is not allowed to be processed.
	 * Default implementation just sends a {@linkplain HttpServletResponse#SC_FORBIDDEN} error
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws IOException if an input or output error occurs while handling the HTTP request
	 * @throws ServletException if the HTTP request cannot be handled
	 */
	protected void onNotAllowed(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		sendError(resp, HttpServletResponse.SC_FORBIDDEN);
	}
	
	@Override
	public final void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (securityHandler != null) {
			if (securityHandler.isAllowed(req, resp)) {
				beforeAction(req, resp);
				onProcessRequest(req, resp);
				afterAction(req, resp);
			} else {
				onNotAllowed(req, resp);
			}
		} else {
			onProcessRequest(req, resp);
		}
	}
	
	/**
	 * Sends an error to the client.
	 * Default implementation uses container's error mechanism if available
	 * @param resp HTTP response
	 * @param status status code
	 * @throws IOException if an input or output error occurs while sending the HTTP response
	 */
	protected void sendError(HttpServletResponse resp, int status) throws IOException {
		resp.sendError(status);
	}
}
