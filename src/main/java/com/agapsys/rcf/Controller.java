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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private static final Set<String> EMPTY_ROLE_SET = Collections.unmodifiableSet(new LinkedHashSet<String>());
	private static final Object[] EMPTY_OBJ_ARRAY = new Object[] {};

	private static class MethodActionValidator {

		private static final Class[] SUPPORTED_CLASSES = new Class[] {
			HttpExchange.class,
			HttpServletRequest.class,
			HttpServletResponse.class,
			HttpRequest.class,
			HttpResponse.class
		};

		/**
		 * Returns a boolean indicating if a class is supported as an argument of an action method.
		 *
		 * @param tested tested class
		 * @param supportedClasses supported classes
		 * @return a boolean indicating if a class is supported as an argument of an action method.
		 */
		public static boolean isSupported(Class tested) {
			for (Class c : SUPPORTED_CLASSES) {
				if (c.isAssignableFrom(tested)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Checks if an annotated method signature matches with required one.
		 *
		 * @param method annotated method.
		 * @return boolean indicating if method signature is valid.
		 */
		public static boolean matchSignature(Method method) {
			String signature = method.toGenericString();
			String[] tokens = signature.split(Pattern.quote(" "));

			if (!tokens[0].equals("public")) {
				return false;
			}

			int indexOfOpenParenthesis = signature.indexOf("(");
			int indexOfCloseParenthesis = signature.indexOf(")");

			String args = signature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis).trim();
			if (args.indexOf(",") != -1) {
				return false; // <-- only one arg method is accepted
			}
			if (args.isEmpty()) {
				return true; // <-- accepts no args
			}


			try {
				Class<?> clazz = Class.forName(args);
				return isSupported(clazz);
			} catch (ClassNotFoundException ex) {
				return false;
			}
		}

		public static Object[] getCallParams(Method method, HttpExchange exchange) {
			if (method.getParameterCount() == 0) return EMPTY_OBJ_ARRAY;

			Class<?> type = method.getParameterTypes()[0];

			if (HttpExchange.class.isAssignableFrom(type))
				return new Object[] {exchange};

			if (HttpServletRequest.class.isAssignableFrom(type))
				return new Object[] {exchange.getCoreRequest()};

			if (HttpServletResponse.class.isAssignableFrom(type))
				return new Object[] {exchange.getCoreResponse()};

			if (HttpRequest.class.isAssignableFrom(type))
				return new Object[] {exchange.getRequest()};

			if (HttpResponse.class.isAssignableFrom(type))
				return new Object[] {exchange.getResponse()};

			throw new UnsupportedOperationException(String.format("Unsupported param type: %s", type.getName()));
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private class MethodCallerAction implements Action {

		private final String[] requiredRoles;
		private final Method method;

		private MethodCallerAction(Method method, boolean secured, String[] requiredUserRoles) {
			if (method == null) {
				throw new IllegalArgumentException("Method cannot be null");
			}

			if (secured && requiredUserRoles == null) {
				throw new IllegalArgumentException("requiredUserRoles cannot be null");
			}

			this.method = method;
			this.requiredRoles = requiredUserRoles;
		}

		/**
		 * Creates an unprotected action.
		 *
		 * @param method method associated with the action.
		 */
		public MethodCallerAction(Method method) {
			this(method, false, null);
		}

		/**
		 * Creates a secured action.
		 *
		 * @param method method associated with the action.
		 * @param requiredUserRoles required user roles in order to process the
		 * action.
		 */
		public MethodCallerAction(Method method, String[] requiredUserRoles) {
			this(method, true, requiredUserRoles);
		}


		private void checkSecurity(HttpExchange exchange) throws Throwable {
			if (requiredRoles != null) {
				User user = exchange.getCurrentUser();

				if (user == null) {
					throw new UnauthorizedException("Unauthorized");
				}

				if (requiredRoles.length > 0) {
					Set<String> userRoles = user.getRoles();
					if (userRoles == null) {
						userRoles = EMPTY_ROLE_SET;
					}

					for (String requiredUserRole : requiredRoles) {
						if (!userRoles.contains(requiredUserRole)) {
							throw new ForbiddenException("Forbidden");
						}
					}
				}
			}
		}

		private Object getSingleDto(Object obj) {
			if (obj == null) {
				return null;
			}

			if (obj instanceof Dto) {
				return ((Dto) obj).getDto();
			}

			return obj;
		}

		private List getDtoList(List objList) {
			List dto = new LinkedList();

			for (Object obj : objList) {
				dto.add(getSingleDto(obj));
			}

			return dto;
		}

		private Map getDtoMap(Map<Object, Object> objMap) {
			Map dto = new LinkedHashMap();

			for (Map.Entry entry : objMap.entrySet()) {
				dto.put(getSingleDto(entry.getKey()), getSingleDto(entry.getValue()));
			}

			return dto;
		}

		private Set getDtoSet(Set objSet) {
			Set dto = new LinkedHashSet();

			for (Object obj : objSet) {
				dto.add(getSingleDto(obj));
			}

			return dto;
		}

		private Object getDtoObject(Object src) {

			Object dto;

			if (src instanceof List) {
				dto = getDtoList((List) src);
			} else if (src instanceof Set) {
				dto = getDtoSet((Set) src);
			} else if (src instanceof Map) {
				dto = getDtoMap((Map<Object, Object>) src);
			} else {
				dto = getSingleDto(src);
			}

			return dto;
		}


		@Override
		public void processRequest(HttpExchange exchange) throws Throwable {
			try {
				checkSecurity(exchange);

				Object[] callParams = MethodActionValidator.getCallParams(method, exchange);

				Object returnedObj = method.invoke(Controller.this, callParams);

				if (returnedObj == null && method.getReturnType().equals(Void.TYPE))
					return;

				exchange.getResponse().writeObject(getDtoObject(returnedObj));

			} catch (InvocationTargetException | IllegalAccessException ex) {
				if (ex instanceof InvocationTargetException) {
					Throwable targetException = ((InvocationTargetException) ex).getTargetException();

					if (targetException instanceof ClientException) {
						throw (ClientException) targetException;
					} else {
						throw new RuntimeException(targetException);
					}
				}

				throw new RuntimeException(ex);
			}
		}
		
	}

	/**
	 * Registers action methods.
	 */
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
					webActions = new WebAction[]{};
				} else {
					webActions = new WebAction[]{webAction};
				}
			} else {
				webActions = webActionsAnnotation.value();
			}

			for (WebAction webAction : webActions) {
				if (!MethodActionValidator.matchSignature(method)) {
					throw new RuntimeException(String.format("Invalid action signature (%s).", method.toGenericString()));
				}

				HttpMethod[] httpMethods = webAction.httpMethods();
				String path = webAction.mapping().trim();

				if (path.isEmpty()) {
					path = method.getName();
				}

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
	 * Called during controller initialization. Default implementation does nothing.
	 */
	protected void onControllerInit() {}

	/**
	 * Called upon controller uncaught error.
	 *
	 * @param exchange HTTP exchange.
	 * @param throwable uncaught error.
	 * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
	 * @throws ServletException if the HTTP request cannot be handled.
	 * @return a boolean indicating if given error was handled. Default implementation returns false.
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

		if (!onControllerError(exchange, cause)) {
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}

			if (cause instanceof ServletException) {
				throw (ServletException) cause;
			}

			if (cause instanceof IOException) {
				throw (IOException) cause;
			}

			throw new ServletException(cause);
		} else {
			return true;
		}
	}
	// =========================================================================

}
