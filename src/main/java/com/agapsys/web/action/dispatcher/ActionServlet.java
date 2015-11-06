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
public class ActionServlet extends HttpServlet implements ActionService {
	// CLASS SCOPE =============================================================
	/** Default User manager. */
	private static final UserManager DEFAULT_USER_MANAGER = new CsrfUserManager();
	
	/** Default HttpExchange implementation. */
	static class DefaultHttpExchange implements HttpExchange {
		private final ActionServlet servlet;
		private final HttpServletRequest req;
		private final HttpServletResponse resp;

		public DefaultHttpExchange(ActionServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
			this.servlet = servlet;
			this.req = req;
			this.resp = resp;
		}

		public final ActionServlet getServlet() {
			return servlet;
		}
		
		@Override
		public HttpServletRequest getRequest() {
			return req;
		}

		@Override
		public HttpServletResponse getResponse() {
			return resp;
		}

		@Override
		public SessionUser getSessionUser() {
			return getServlet().getUserManager().getSessionUser(this);
		}

		@Override
		public String toString() {
			return String.format("%s %s %s", req.getMethod(), req.getRequestURI(), req.getProtocol());
		}
	}
	
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
	private final ActionDispatcher dispatcher = new ActionDispatcher();
	private final LazyInitializer actionServlet = new LazyInitializer() {
		@Override
		protected void onInitialize(Object...params) {
			ActionServlet.this._onInit();
		}
	};
	private final LazyInitializer<UserManager> userManager = new LazyInitializer<UserManager>() {

		@Override
		protected UserManager getLazyInstance(Object... params) {
			return ActionServlet.this._getUserManager();
		}
		
	};
	
	// CUSTOMIZABLE INITIALIZATION BEHAVIOUR -----------------------------------
	/** Called during servlet initialization. Always call super implementation. */
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
						String[] requiredRoles = webAction.requiredRoles();
						Set<String> requiredRoleSet = new LinkedHashSet<>();

						for (String role : requiredRoles) {
							if (!requiredRoleSet.add(role))
								throw new RuntimeException("Duplicate role: " + role);
						}

						HttpMethod[] httpMethods = webAction.httpMethods();
						String url = webAction.mapping();

						if (url.trim().isEmpty())
							url = method.getName();

						if (!url.startsWith("/"))
							url = "/" + url;

						SecurityManager securityManager = ActionServlet.this._getSecurityManager(requiredRoleSet);
						MethodCallerAction methodCallerAction = new MethodCallerAction(this, method, securityManager);

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
	
	/**
	 * Returns the security manager used by {@linkplain MethodCallerAction} instances.
	 * This method is intended to be overridden to change servlet initialization and not be called directly
	 * @param requiredRoles action required roles
	 * @return the security manger user by created actions managed by this servlet.
	 */
	protected SecurityManager _getSecurityManager(Set<String> requiredRoles) {
		if (requiredRoles == null || requiredRoles.isEmpty()) { // Ignores CSRF token if there is no required roles
			return null;
		} else {
			Set<SecurityManager> securityManagerSet = new LinkedHashSet<>();
			securityManagerSet.add(((CsrfUserManager)getUserManager())._getCsrfSecurityManager());

			final UserRoleSecurityManager userRoleSecurityManager = new UserRoleSecurityManager(getUserManager(), requiredRoles);
			securityManagerSet.add(userRoleSecurityManager);

			return new SecurityManagerSet(securityManagerSet);
		}
	}
	
	/**
	 * Return the user manager instance managed by this servlet.
	 * This method is intended to be overridden to change servlet initialization and not be called directly
	 * @return the user manager instance managed by this servlet.
	 */
	protected UserManager _getUserManager() {
		return DEFAULT_USER_MANAGER;
	}
	// -------------------------------------------------------------------------
	
	/** Called during servlet initialization. Default implementation does nothing. */
	protected void onInit() {}
	
	/** 
	 * Called before an action. 
	 * This method will be called only if an action associated to given request is found and it it allowed to be processed (see {@link SecurityManager}).
	 * Default implementation does nothing.
	 * @param exchange HTTP exchange
	 */
	@Override
	public void beforeAction(HttpExchange exchange) {}
		
	/** 
	 * Called after an action. 
	 * This method will be called only if an action associated to given request is found, the action is allowed to be processed (see {@link SecurityManager}), and the action was successfully processed.
	 * Default implementation does nothing.
	 * @param exchange HTTP exchange
	 */
	@Override
	public void afterAction(HttpExchange exchange) {}
	
	/** 
	 * Called when an action is not found.
	 * An action is not found when there is no method mapped to given request.
	 * Default implementation sets a {@linkplain HttpServletResponse#SC_NOT_FOUND} status in the response.
	 * @param exchange HTTP exchange
	 */
	@Override
	public void onNotFound(HttpExchange exchange) {
		exchange.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
	/** 
	 * Handles an error in the application and returns a boolean indicating if error shall be propagated.
	 * @param exchange HTTP exchange
	 * @param throwable error
	 * @return a boolean indicating if given error shall be propagated. Default implementation just returns true.
	 */
	@Override
	public boolean onError(HttpExchange exchange, Throwable throwable) {
		return true;
	}
	
	/**
	 * Called when an action is not allowed to be executed.
	 * Default implementation sets a status in the response:
	 * <ul>
	 *		<li> {@linkplain HttpServletResponse#SC_UNAUTHORIZED} if called action has required roles an there is no user registered with request session.</li>
	 *		<li> {@linkplain HttpServletResponse#SC_FORBIDDEN} if there is an user registered with request session but the user does not fulfill required roles</li>
	 * </ul>
	 * @param exchange HTTP exchange
	 * @see ActionServlet#getUserManager()
	 */
	@Override
	public void onNotAllowed(HttpExchange exchange) {
		SessionUser sessionUser = getUserManager().getSessionUser(exchange);
		
		if (sessionUser == null) {
			exchange.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			exchange.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
	
	/**
	 * Return The HTTP exchange used by this servlet
	 * @return The HTTP exchange used by this servlet
	 * @param req HTTP request
	 * @param resp HTTP response
	 */
	protected HttpExchange getHttpExchange(HttpServletRequest req, HttpServletResponse resp) {
		return new DefaultHttpExchange(this, req, resp);
	}
	
	/**
	 * Returns the user manager used by this servlet.
	 * @return the user manager used by this servlet
	 */
	public final UserManager getUserManager() {
		return userManager.getInstance();
	}
	
	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) {
		if (!actionServlet.isInitialized())
			actionServlet.initialize();
		
		Action action = dispatcher.getAction(req);
		
		HttpExchange exchange = getHttpExchange(req, resp);
		
		if (action == null) {
			onNotFound(exchange);
		} else {
			try {
				action.processRequest(exchange);
			} catch (RuntimeException t) { // MethodCallerAction throws the target exception wrapped in a RuntimeException
				Throwable cause = t.getCause();
				if (onError(exchange, cause))
					throw new RuntimeException(cause);
			}
		}
	}
	// =========================================================================
}
