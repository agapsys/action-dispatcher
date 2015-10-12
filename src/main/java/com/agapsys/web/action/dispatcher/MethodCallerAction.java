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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Action responsible by calling {@linkplain ActionService} methods annotated with {@linkplain WebAction} or {@linkplain WebActions}.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class MethodCallerAction extends AbstractAction {
	private final Method method;
	private final ActionService actionService;
	
	/**
	 * Constructor.
	 * @param actionService Action service instance associated with this action caller.
	 * @param method mapped method
	 * @param securityManager the security manager used by action
	 */
	public MethodCallerAction(ActionService actionService, Method method, SecurityManager securityManager) {
		super(securityManager);
		
		if (actionService == null)
			throw new IllegalArgumentException("Null servlet");
		
		if (method == null)
			throw new IllegalArgumentException("Null method");
		
		this.actionService = actionService;
		this.method = method;
	}

	/** 
	 * Returns the action service.
	 * @return the action service passed in constructor.
	 */
	public final ActionService getActionService() {
		return actionService;
	}

	@Override
	protected void beforeAction(HttpExchange exchange){
		actionService.beforeAction(exchange);
	}

	@Override
	protected void afterAction(HttpExchange exchange){
		actionService.afterAction(exchange);
	}
	
	@Override
	protected void onNotAllowed(HttpExchange exchange){
		actionService.onNotAllowed(exchange);
	}

	@Override
	protected void onProcessRequest(HttpExchange exchange) {
		try {
			method.invoke(actionService, exchange);
		} catch (InvocationTargetException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
}
