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

import com.agapsys.rcf.exceptions.BadRequestException;
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
public class Controller extends ActionServlet {

	// CLASS SCOPE =============================================================	
	public static final String SESSION_ATTR_USER = Controller.class.getName() + ".sessionUser";
	
	private static final String[] EMPTY_STR_ARRAY = new String[] {};
	
	public static final ObjectSerializer DEFAULT_SERIALIZER = new GsonSerializer();
	
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
		return args.isEmpty() || args.equals(HttpServletRequest.class.getName()) || args.equals(HttpServletResponse.class.getName()) ||  args.equals(HttpExchange.class.getName());
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final LazyInitializer<ObjectSerializer> serializer = new LazyInitializer<ObjectSerializer>() {

		@Override
		protected ObjectSerializer getLazyInstance() {
			return getCustomSerializer();
		}

	};

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
				User user = getUser(exchange.getRequest());
				
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

					if (type == HttpExchange.class) {
						passedParam = exchange;
					} else if (type == HttpServletRequest.class) {
						passedParam = exchange.getRequest();
					} else {
						passedParam = exchange.getResponse();
					}
				} else {
					passedParam = null;
				}
				
				Object[] args = passedParam != null ? new Object[] {passedParam} : new Object[] {};
				
				Object returnedObj = method.invoke(Controller.this, args);
				if (returnedObj == null && method.getReturnType().equals(Void.TYPE))
					return;
				
				serializer.getInstance().writeObject(exchange.getResponse(), returnedObj);

			} catch (InvocationTargetException | IllegalAccessException ex) {
				if (ex instanceof InvocationTargetException) {
					throw new RuntimeException(((InvocationTargetException) ex).getTargetException());
				}

				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Returns control's default object serializer. This method will be called
	 * only once.
	 *
	 * @return control's default object serializer.
	 */
	protected ObjectSerializer getCustomSerializer() {
		return DEFAULT_SERIALIZER;
	}

	/**
	 * Called during controller initialization. Subclasses shall call this
	 * implementation.
	 */
	@Override
	protected void onInit() {
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
	}

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
	protected boolean onControllerError(HttpExchange exchange, Throwable throwable) throws ServletException, IOException {
		return false;
	}

	/**
	 * Reads an objected send with the request
	 * @param <T> object type
	 * @param req HTTP request
	 * @param targetClass object class
	 * @return read object
	 * @throws BadRequestException if it was not possible to read an object of given type.
	 */
	protected <T> T readObject(HttpServletRequest req, Class<T> targetClass) throws BadRequestException {
		return serializer.getInstance().readObject(req, targetClass);
	}
	
	/**
	 * Writes an object into response
	 * 
	 * @param resp HTTP response
	 * @param obj object to be written
	 * @throws IOException if an error happened during writing operation
	 */
	protected void writeObject(HttpServletResponse resp, Object obj) throws IOException {
		serializer.getInstance().writeObject(resp, obj);
	}
	
	/**
	 * Returns the user associated with given request.
	 * @param req HTTP request.
	 * @return the user associated with given request. Default implementation returns the user stored in session attribute {@linkplain Controller#SESSION_ATTR_USER}.
	 */
	protected User getUser(HttpServletRequest req) {
		return (User) req.getSession().getAttribute(SESSION_ATTR_USER);
	}
	
	/**
	 * Sets the user associated with a request.
	 * @param user application user.
	 * @param exchange HTTP exchange.
	 */
	protected void setUser(HttpExchange exchange, User user) {
		exchange.getRequest().getSession().setAttribute(SESSION_ATTR_USER, user);
	}
	
	@Override
	protected final boolean onUncaughtError(HttpExchange exchange, Throwable throwable) throws ServletException, IOException {
		super.onUncaughtError(exchange, throwable);

		Throwable cause = throwable.getCause(); // <-- MethodCallerAction throws the target exception wrapped in a RuntimeException
		
		if (cause == null) {
			cause = throwable;
		}

		HttpServletRequest req = exchange.getRequest();
		HttpServletResponse resp = exchange.getResponse();

		if (cause instanceof ClientException) {
			ClientException ex = (ClientException) cause;

			onClientError(req, ex);

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

