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

import com.agapsys.rcf.exceptions.ClientException;
import com.agapsys.rcf.exceptions.ForbiddenException;
import com.agapsys.rcf.exceptions.UnauthorizedException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet responsible by mapping methods to actions
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class Controller<HE extends HttpExchange> extends ActionServlet<HE> {

	// CLASS SCOPE =============================================================	
	private static final String[] EMPTY_STR_ARRAY = new String[] {};
		
	/**
	 * Checks if an annotated method signature matches with required one.
	 *
	 * @param method annotated method
	 * @return boolean indicating if method signature is valid.
	 */
	private static boolean matchSignature(Method method) {
		String signature = method.toGenericString();
		String[] tokens = signature.split(Pattern.quote(" "));

		if (!tokens[0].equals("public")) return false;

		int indexOfOpenParenthesis = signature.indexOf("(");
		int indexOfCloseParenthesis = signature.indexOf(")");

		String args = signature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis).trim();
		if (args.indexOf(",") != -1) return false; // <-- only one arg method is accepted
		if (args.isEmpty()) return true; // <-- accepts no args 
		
		try {
			Class<?> clazz = Class.forName(args);
			return HttpExchange.class.isAssignableFrom(clazz) || HttpServletRequest.class.isAssignableFrom(clazz) || HttpServletResponse.class.isAssignableFrom(clazz);			
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private class MethodCallerAction implements Action {
		private final String[] requiredRoles;
		private final Method   method;

		private MethodCallerAction(Method method, boolean secured, String[] requiredUserRoles) {
			if (method == null)
				throw new IllegalArgumentException("Method cannot be null");

			if (secured && requiredUserRoles == null)
				throw new IllegalArgumentException("requiredUserRoles cannot be null");
			
			this.method = method;
			this.requiredRoles = requiredUserRoles;
		}
		
		/**
		 * Creates an unprotected action.
		 * @param method method associated with the action.
		 */
		public MethodCallerAction(Method method) {
			this(method, false, null);
		}

		/**
		 * Creates a secured action.
		 * @param method method associated with the action.
		 * @param requiredUserRoles required user roles in order to process the action.
		 */
		public MethodCallerAction(Method method, String[] requiredUserRoles) {
			this(method, true, requiredUserRoles);
		}
		
		private boolean belongsToArray(String test, String[] array) {
			for (String element : array) {
				if (test.equals(element))
					return true;
			}
			
			return false;
		}
		
		private void checkSecurity(HttpExchange exchange) throws Throwable {
			if (requiredRoles != null) {
				User user = exchange.getCurrentUser();
				
				if (user == null)
					throw new UnauthorizedException();
				
				if (requiredRoles.length > 0) {
					String[] userRoles = user.getRoles();
					if (userRoles == null) userRoles = EMPTY_STR_ARRAY;
					
					for (String requiredUserRole : requiredRoles) {
						if (!belongsToArray(requiredUserRole, userRoles))
							throw new ForbiddenException();
					}
				}
			}
		}
		
		@Override
		public void processRequest(HttpExchange exchange) throws Throwable {
			try {
				checkSecurity(exchange);
				
				Object passedParam;
				
				if (method.getParameterCount() > 0) {
					Class<?> type = method.getParameterTypes()[0];

					if (HttpExchange.class.isAssignableFrom(type)) {
						passedParam = exchange;
					} else if (HttpServletRequest.class.isAssignableFrom(type)) {
						passedParam = exchange.getRequest();
					} else if (HttpServletResponse.class.isAssignableFrom(type)) {
						passedParam = exchange.getResponse();
					} else {
						throw new RuntimeException("Unsupported arg type: " + type.getName());
					}
				} else {
					passedParam = null;
				}
				
				Object[] args = passedParam != null ? new Object[] {passedParam} : new Object[] {};
				
				Object returnedObj = method.invoke(Controller.this, args);
				if (returnedObj == null && method.getReturnType().equals(Void.TYPE))
					return;
				
				exchange.writeObject(returnedObj);

			} catch (InvocationTargetException | IllegalAccessException ex) {
				if (ex instanceof InvocationTargetException) {
					throw new RuntimeException(((InvocationTargetException) ex).getTargetException());
				}

				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	protected final void onInit() {
		super.onInit();
		
		Class<? extends Controller> actionServletClass = Controller.this.getClass();
		
		// Check for WebAction annotations...
		Method[] methods = actionServletClass.getDeclaredMethods();

		for (Method method : methods) {
			WebActions webActionsAnnotation = method.getAnnotation(WebActions.class);
			WebAction[] webActions;
			
			if (webActionsAnnotation == null) {
				WebAction webAction = method.getAnnotation(WebAction.class);
				if (webAction == null) {
					webActions = new WebAction[] {};
				} else {
					webActions = new WebAction[] {webAction};
				}
			} else {
				webActions = webActionsAnnotation.value();
			}
			
			for (WebAction webAction : webActions) {
				if (!matchSignature(method))
					throw new RuntimeException(String.format("Invalid action signature (%s).", method.toGenericString()));

				HttpMethod[] httpMethods = webAction.httpMethods();
				String path = webAction.mapping().trim();

				if (path.isEmpty())
					path = method.getName();

				MethodCallerAction action;
				
				boolean isSecured = webAction.secured() || webAction.requiredRoles().length > 0;

				if (!isSecured) {
					action = new MethodCallerAction(method);
				} else {
					action = new MethodCallerAction(method, webAction.requiredRoles());
				}

				for (HttpMethod httpMethod : httpMethods) {
					registerAction(httpMethod, path, action);

					if (webAction.defaultAction()) {
						registerAction(httpMethod, ActionDispatcher.ROOT_PATH, action);
					}
				}
			}
		}
		
		onControllerInit();
	}

	/**
	 * Called during controller initialization.
	 */
	protected void onControllerInit() {}
	
	/**
	 * Called upon controller uncaught error.
	 *
	 * @param exchange HTTP exchange
	 * @param throwable uncaught error
	 * @throws IOException if an input or output error occurs while the servlet
	 * is handling the HTTP request.
	 * @throws ServletException if the HTTP request cannot be handled
	 * @return a boolean indicating if given error was handled. Default
	 * implementation returns false
	 */
	protected boolean onControllerError(HE exchange, Throwable throwable) throws ServletException, IOException {
		return false;
	}

	@Override
	protected final boolean onUncaughtError(HE exchange, Throwable throwable) throws ServletException, IOException {
		super.onUncaughtError(exchange, throwable);

		Throwable cause = throwable.getCause(); // <-- MethodCallerAction throws the target exception wrapped in a RuntimeException
		
		if (cause == null) {
			cause = throwable;
		}

		HttpServletResponse resp = exchange.getResponse();

		if (cause instanceof ClientException) {
			ClientException ex = (ClientException) cause;

			onClientError(exchange, ex);

			resp.setStatus(ex.getHttpsStatus());
			Integer appStatus = ex.getAppStatus();
			resp.getWriter().printf(
				"%s%s",
				appStatus == null ? "" : String.format("%d:", appStatus),
				ex.getMessage()
			);
			
			return true;
			
		} else {
			if (!onControllerError(exchange, cause)) {
				if (cause instanceof RuntimeException)
					throw (RuntimeException) cause;

				if (cause instanceof ServletException)
					throw (ServletException) cause;

				if (cause instanceof IOException)
					throw (IOException) cause;

				throw new ServletException(cause);
			} else {
				return true;
			}
		}
	}
	// =========================================================================
	
}

