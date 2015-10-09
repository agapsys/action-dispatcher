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
 * Action caller used by action servlet.
 * Each method annotated with {@linkplain WebAction} or {@linkplain WebActions}
 * will be internally called by a instance of an action caller.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ActionCaller extends AbstractAction {
	private final Method method;
	private final ActionServlet servlet;
	
	/**
	 * Constructor.
	 * @param servlet Action servlet responsible by handling of this action caller
	 * @param method mapped method
	 * @param securityHandler the security handler used by action
	 */
	public ActionCaller(ActionServlet servlet, Method method, SecurityHandler securityHandler) {
		super(securityHandler);
		
		if (servlet == null)
			throw new IllegalArgumentException("Null servlet");
		
		if (method == null)
			throw new IllegalArgumentException("Null method");
		
		this.servlet = servlet;
		this.method = method;
	}

	/** 
	 * Returns the servlet passed in constructor.
	 * @return the servlet passed in constructor.
	 */
	public final ActionServlet getServlet() {
		return servlet;
	}

	@Override
	protected void beforeAction(RequestResponsePair rrp){
		getServlet().beforeAction(rrp);
	}

	@Override
	protected void afterAction(RequestResponsePair rrp){
		getServlet().afterAction(rrp);
	}
	
	@Override
	protected void onNotAllowed(RequestResponsePair rrp){
		getServlet().onNotAllowed(rrp);
	}

	@Override
	protected void onProcessRequest(RequestResponsePair rrp){
		try {
			method.invoke(getServlet(), rrp);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException | IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void sendError(RequestResponsePair rrp, int status){
		getServlet().sendError(rrp, status);
	}
}
