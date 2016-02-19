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

package com.agapsys.rcf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Action responsible by calling {@link Controller} methods annotated with {@linkplain WebAction} or {@linkplain WebActions}.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
class MethodCallerAction extends AbstractAction {
	private final Method method;
	private final Controller actionServlet;
	
	/**
	 * Constructor.
	 * @param actionServlet Action servlet instance associated with this action caller.
	 * @param method mapped method
	 */
	public MethodCallerAction(Controller actionServlet, Method method) {
		if (actionServlet == null)
			throw new IllegalArgumentException("Action servlet cannot be null");
		
		if (method == null)
			throw new IllegalArgumentException("Method cannot be null");
		
		this.actionServlet = actionServlet;
		this.method = method;
	}

	/** 
	 * Returns the action service.
	 * @return the action service passed in constructor.
	 */
	public final Controller getActionServlet() {
		return actionServlet;
	}

	@Override
	protected void beforeAction(HttpExchange exchange){
		actionServlet.beforeAction(exchange);
	}

	@Override
	protected void afterAction(HttpExchange exchange){
		actionServlet.afterAction(exchange);
	}

	@Override
	protected void onProcessRequest(HttpExchange exchange) {
		try {
			method.invoke(actionServlet, exchange);
		} catch (InvocationTargetException | IllegalAccessException ex) {
			if (ex instanceof InvocationTargetException)
				throw new RuntimeException(((InvocationTargetException) ex).getTargetException());
			
			throw new RuntimeException(ex);
		}
	}
}
