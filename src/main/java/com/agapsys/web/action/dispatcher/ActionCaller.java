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
import javax.servlet.http.HttpServletResponse;

/**
 * Action caller used by action servlet.
 * Each method annotated with {@linkplain WebAction} or {@linkplain WebActions}
 * will be internally called by a instance of an action caller.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ActionCaller extends AbstractAction {
	// CLASS SCOPE =============================================================
	/** Custom action caller to handle {@linkplain DataBindRequest} methods. */
	static class DataBindActionCaller extends ActionCaller {
		private final Class targetClass;
		private final ObjectSerializer serializer;

		/**
		 * Constructor.
		 * @param serializer object serializer
		 * @param targetClass target class
		 * @param servlet action servlet
		 * @param method mapped method
		 * @param securityHandler security handler
		 */
		public DataBindActionCaller(
			ObjectSerializer serializer,
			Class targetClass, 
			ActionServlet servlet, 
			Method method, 
			SecurityHandler securityHandler
		) {
			super(servlet, method, securityHandler);
			this.targetClass = targetClass;
			this.serializer = serializer;
		}

		@Override
		protected void onProcessRequest(HttpExchange exchange) {
			if (targetClass != null) {
				try {
					Object targetObject = serializer.readObject(exchange, targetClass);
					exchange.getRequest().setAttribute(DataBindController.ATTR_TARGET_OBJECT, targetObject);
					super.onProcessRequest(exchange);
				} catch (ObjectSerializer.BadRequestException ex) {
					exchange.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST); // Skip request if target object cannot be obtained from request
				}
			} else {
				super.onProcessRequest(exchange);
			}
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
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
	protected void beforeAction(HttpExchange exchange){
		getServlet().beforeAction(exchange);
	}

	@Override
	protected void afterAction(HttpExchange exchange){
		getServlet().afterAction(exchange);
	}
	
	@Override
	protected void onNotAllowed(HttpExchange exchange){
		getServlet().onNotAllowed(exchange);
	}

	@Override
	protected void onProcessRequest(HttpExchange exchange){
		try {
			method.invoke(getServlet(), exchange);
		} catch (InvocationTargetException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
	// =========================================================================
}
