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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet responsible by mapping methods to actions
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ActionServlet extends HttpServlet {
	// CLASS SCOPE =============================================================	
	/** 
	 * Checks if an annotated method signature matches with required one.
	 * @param method annotated method
	 * @return boolean indicating if method signature is valid.
	 */
	private static boolean matchSignature(Method method) {
		String signature = method.toGenericString();
		String paramType = HttpExchange.class.getName();
		
		if (!signature.startsWith("public void ")) {
			return false;
		}
		
		int indexOfOpenParenthesis = signature.indexOf("(");
		int indexOfCloseParenthesis = signature.indexOf(")");
		
		String args = signature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis);
		return args.equals(paramType);
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================	
	private final ActionDispatcher dispatcher    = new ActionDispatcher();
	private final LazyInitializer  lazyInitializer = new LazyInitializer() {
		@Override
		protected void onInitialize() {
			ActionServlet.this._onInit();
		}
	};
	
	/** Called during servlet initialization. */
	private void _onInit() {
		Class<? extends ActionServlet> actionServletClass = ActionServlet.this.getClass();
		
		String thisClassName = actionServletClass.getName();

		// Check if this class uses WebServlet annotation...
		WebServlet[] webServletAnnotationArray = actionServletClass.getAnnotationsByType(WebServlet.class);
		if (webServletAnnotationArray.length == 0)
			throw new RuntimeException(String.format("Class '%s' is not annotated with '%s'", thisClassName, WebServlet.class.getName()));

		WebServlet webServletAnnotation = webServletAnnotationArray[0];
		String[] urlPatternArray = webServletAnnotation.urlPatterns();
		if (urlPatternArray.length == 0) {
			urlPatternArray = webServletAnnotation.value();
		}

		if (urlPatternArray.length == 0)
			throw new RuntimeException(String.format("Servlet class '%s' does not have any URL pattern", thisClassName));

		for (String urlPattern : urlPatternArray) {
			if (!urlPattern.endsWith("/*")) {
				throw new RuntimeException(String.format("Invalid URL pattern '%s' for class '%s' (pattern must end with '/*')", urlPattern, thisClassName));
			}
		}
			
		// Check for WebAction annotations...
		Method[] methods = actionServletClass.getDeclaredMethods();
		for (Method method : methods) {
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				if ((annotation instanceof WebAction) || (annotation instanceof WebActions)) {
					if (!matchSignature(method))
						throw new RuntimeException(String.format("Invalid signature (%s). Required: public void <method_name>(%s)", method.toGenericString(), HttpExchange.class.getName()));

					WebAction[] webActions;

					if (annotation instanceof WebActions) {
						webActions = ((WebActions) annotation).value();
					} else {
						webActions = new WebAction[]{(WebAction) annotation};
					}

					for (WebAction webAction : webActions) {
						HttpMethod[] httpMethods = webAction.httpMethods();
						String url = webAction.mapping();

						if (url.trim().isEmpty())
							url = method.getName();

						if (!url.startsWith("/"))
							url = "/" + url;

						MethodCallerAction methodCallerAction = new MethodCallerAction(this, method);

						for (HttpMethod httpMethod : httpMethods) {
							dispatcher.registerAction(methodCallerAction, httpMethod, url);

							if (webAction.defaultAction()) {
								dispatcher.registerAction(methodCallerAction, httpMethod, ActionDispatcher.DEFAULT_URL);
							}
						}
					}
				}
			}
		}
		
		onInit();
	}

	/** Called during servlet initialization. Default implementation does nothing. */
	protected void onInit() {}
	
	/** 
	 * Called before an action. 
	 * This method will be called only if an action associated to given request is found and it it allowed to be processed (see {@link SecurityManager}).
	 * Default implementation does nothing.
	 * @param exchange HTTP exchange
	 */
	protected void beforeAction(HttpExchange exchange) {}
		
	/** 
	 * Called after an action. 
	 * This method will be called only if an action associated to given request is found, the action is allowed to be processed (see {@link SecurityManager}), and the action was successfully processed.
	 * Default implementation does nothing.
	 * @param exchange HTTP exchange
	 */
	protected void afterAction(HttpExchange exchange) {}
	
	/** 
	 * Called when an action is not found.
	 * An action is not found when there is no method mapped to given request.
	 * Default implementation sets a {@linkplain HttpServletResponse#SC_NOT_FOUND} status in the response.
	 * @param exchange HTTP exchange
	 */
	protected void onNotFound(HttpExchange exchange) {
		exchange.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
	/** 
	 * Handles an error in the application and returns a boolean indicating if error shall be propagated.
	 * @param exchange HTTP exchange
	 * @param throwable error
	 * @return a boolean indicating if given error shall be propagated. Default implementation just returns true.
	 */
	protected boolean onError(HttpExchange exchange, Throwable throwable) {
		return true;
	}
	
	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) {
		if (!lazyInitializer.isInitialized())
			lazyInitializer.initialize();
		
		Action action = dispatcher.getAction(req);
		
		HttpExchange exchange = new HttpExchange(req, resp);
		
		if (action == null) {
			onNotFound(exchange);
		} else {
			try {
				action.processRequest(exchange);
			} catch (RuntimeException t) { // MethodCallerAction throws the target exception wrapped in a RuntimeException
				Throwable cause = t.getCause();
				
				if (cause == null)
					cause = t;
				
				if (onError(exchange, cause))
					throw new RuntimeException(cause);
			}
		}
	}
	// =========================================================================
}
