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
import java.io.IOException;
import java.lang.annotation.Annotation;
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
	private static final ObjectSerializer DEFAULT_SERIALIZER = new GsonSerializer();

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

		String args = signature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis);
		return args.equals(HttpServletRequest.class.getName());
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final LazyInitializer<ObjectSerializer> serializer = new LazyInitializer<ObjectSerializer>() {

		@Override
		protected ObjectSerializer getLazyInstance() {
			return getSerializer();
		}

	};

	private class MethodCallerAction implements Action {

		private final Method method;

		/**
		 * Constructor.
		 *
		 * @param method mapped method
		 */
		public MethodCallerAction(Method method) {

			if (method == null)
				throw new IllegalArgumentException("Method cannot be null");

			this.method = method;
		}

		@Override
		public void processRequest(HttpExchange exchange) {
			try {
				Object returnedObj = method.invoke(Controller.this, exchange.getRequest());
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
	protected ObjectSerializer getSerializer() {
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
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				if ((annotation instanceof WebAction) || (annotation instanceof WebActions)) {
					if (!matchSignature(method)) {
						throw new RuntimeException(String.format("Invalid signature (%s). Required: public <DtoObject_or_subclass> <method_name>(%s)", method.toGenericString(), HttpExchange.class.getName()));
					}

					WebAction[] webActions;

					if (annotation instanceof WebActions) {
						webActions = ((WebActions) annotation).value();
					} else {
						webActions = new WebAction[]{(WebAction) annotation};
					}

					for (WebAction webAction : webActions) {
						HttpMethod[] httpMethods = webAction.httpMethods();
						String path = webAction.mapping();

						if (path.trim().isEmpty()) {
							path = method.getName();
						}

						MethodCallerAction action = new MethodCallerAction(method);

						for (HttpMethod httpMethod : httpMethods) {
							registerAction(httpMethod, path, action);

							if (webAction.defaultAction()) {
								registerAction(httpMethod, ActionDispatcher.ROOT_PATH, action);
							}
						}
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

