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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
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
	private static final UserManager DEFAULT_USER_MANAGER = new CsrfUserManager();
	
	private static void matchSignature(Method method) throws RuntimeException {
		String signature = method.toGenericString();
		
		String errMsg = String.format("Invalid signature (%s). Required: public void <method_name>(com.agapsys.web.action.dispatcher.RequestResponsePair)", signature);
		
		if (!signature.startsWith("public void "))
			throw new RuntimeException(errMsg);
		
		int indexOfOpenParenthesis = signature.indexOf("(");
		int indexOfCloseParenthesis = signature.indexOf(")");
		
		String args = signature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis);
		if (!args.equals("com.agapsys.web.action.dispatcher.RequestResponsePair"))
			throw new RuntimeException(errMsg);
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================	
	private final ActionDispatcher dispatcher = new ActionDispatcher();
	private final LazyInitializer lazyInitializer = new LazyInitializer() {
		@Override
		protected void onInitialize() {
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
						matchSignature(method);
								
						WebAction[] webActions;
						
						if (annotation instanceof WebActions) {
							webActions = ((WebActions) annotation).value();
						} else {
							webActions = new WebAction[]{(WebAction) annotation};
						}

						for (WebAction webAction : webActions) {
							String[] requiredRoles = webAction.requiredRoles();
							Set<String> requiredRoleSet = new LinkedHashSet<>();

							for (String role : requiredRoles) {
								if (!requiredRoleSet.add(role))
									throw new RuntimeException("Duplicate role: " + role);
							}

							HttpMethod httpMethod = webAction.httpMethod();
							String url = webAction.mapping();

							if (url.trim().isEmpty())
								url = method.getName();

							if (!url.startsWith("/"))
								url = "/" + url;

							SecurityHandler handler = getSecurityHandler(requiredRoleSet);
							ActionCaller actionCaller = getActionCaller(method, handler);
							dispatcher.registerAction(actionCaller, httpMethod, url);

							if (webAction.defaultAction()) {
								dispatcher.registerAction(actionCaller, httpMethod, ActionDispatcher.DEFAULT_URL);
							}
						}
					}
				}
			}
		}
	};
	
	/**
	 * Returns the action caller which will be responsible by call a method in servlet.
	 * This method is called during servlet initialization when there is a method annotated with either {@linkplain WebAction} or {@linkplain WebActions}
	 * @param method annotated method
	 * @param securityHandler security handler used by mapped method
	 * @return action caller which will actually call a servlet method
	 */
	protected ActionCaller getActionCaller(Method method, SecurityHandler securityHandler) {
		return new ActionCaller(this, method, securityHandler);
	}
	
	/** 
	 * Called before an action. 
	 * This method will be called only if an action associated to given request is found and it it allowed to be processed (see {@linkplain SecurityHandler}).
	 * Default implementation does nothing.
	 * @param rrp request-response pair
	 */
	protected void beforeAction(RequestResponsePair rrp) {}
	
	/** 
	 * Called after an action. 
	 * This method will be called only if an action associated to given request is found, the action is allowed to be processed (see {@linkplain SecurityHandler}), and the action was successfully processed.
	 * Default implementation does nothing.
	 * @param rrp request-response pair
	 */
	protected void afterAction(RequestResponsePair rrp) {}
	
	/** 
	 * Called when an action is not found.
	 * An action is not found when there is no method mapped to given request.
	 * Default implementation sends a {@linkplain HttpServletResponse#SC_NOT_FOUND} error.
	 * @param rrp request-response pair
	 */
	protected void onNotFound(RequestResponsePair rrp) {
		sendError(rrp, HttpServletResponse.SC_NOT_FOUND);
	}
	
	/** 
	 * Called when there is an error processing an action.
	 * Default implementation just throws given exception.
	 * @param throwable error
	 * @param rrp request-response pair
	 */
	protected void onError(Throwable throwable, RequestResponsePair rrp) {
		if (throwable instanceof RuntimeException)
			throw (RuntimeException) throwable;
		
		throw new RuntimeException(throwable);
	}
	
	/**
	 * Called when an action is not allowed to be executed.
	 * Default implementation sends:
	 * <ul>
	 *		<li> {@linkplain HttpServletResponse#SC_UNAUTHORIZED} if called action has required roles an there is no user registered with request session.</li>
	 *		<li> {@linkplain HttpServletResponse#SC_FORBIDDEN} if there is an user registered with request session but the user does not fulfill required roles</li>
	 * </ul>
	 * @param rrp request-response pair
	 * @see ActionServlet#getUserManager()
	 */
	protected void onNotAllowed(RequestResponsePair rrp) {
		ApplicationUser sessionUser = getUserManager().getSessionUser(rrp);
		
		if (sessionUser == null) {
			sendError(rrp, HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			sendError(rrp, HttpServletResponse.SC_FORBIDDEN);
		}
	}
	
	/**
	 * Returns the security manager used by an action handled by this servlet.
	 * Default implementation returns a non-null security handler if there is no required roles. Otherwise, returns null
	 * @param requiredRoles action required roles
	 * @return the security manager used by an action
	 */
	protected SecurityHandler getSecurityHandler(Set<String> requiredRoles) {
		if (requiredRoles == null || requiredRoles.isEmpty()) { // Ignores CSRF token if there is no required roles
			return null;
		} else {
			Set<SecurityHandler> handlerSet = new LinkedHashSet<>();
			handlerSet.add(((CsrfUserManager)getUserManager()).getCsrfSecurityHandler());

			final UserRoleSecurityHandler userRoleSecurityHandler = new UserRoleSecurityHandler(getUserManager(), requiredRoles);
			handlerSet.add(userRoleSecurityHandler);

			return new SecurityHandlerSet(handlerSet);
		}
	}
		
	/**
	 * Returns the user manager used by this servlet.
	 * <b>ATTENTION:</b>This method may be called multiple times during runtime. Do not create a new instance after each call in order to improve performance.
	 * Default implementation returns a default instance of {@linkplain CsrfUserManager}
	 * @return the user manager used by this servlet
	 */
	protected UserManager getUserManager() {
		return DEFAULT_USER_MANAGER;
	}
	
	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) {
		if (!lazyInitializer.isInitialized())
			lazyInitializer.initialize();
		
		Action action = dispatcher.getAction(req);
		
		RequestResponsePair rrp = new RequestResponsePair(req, resp);
		
		if (action == null) {
			onNotFound(rrp);
		} else {
			try {
				action.processRequest(rrp);
			} catch (Throwable t) {
				onError(t, rrp);
			}
		}
	}
	
	/**
	 * Sends an error to the client.
	 * Default implementation uses container's error mechanism if available
	 * @param rrp request-response pair
	 * @param status status code
	 */
	protected void sendError(RequestResponsePair rrp, int status) {
		try {
			rrp.getResponse().sendError(status);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	// =========================================================================
}
