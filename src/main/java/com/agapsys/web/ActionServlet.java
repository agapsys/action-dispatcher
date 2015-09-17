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

package com.agapsys.web;

import com.agapsys.web.annotations.AfterAction;
import com.agapsys.web.annotations.BeforeAction;
import com.agapsys.web.annotations.NotFoundAction;
import com.agapsys.web.annotations.WebAction;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionServlet extends HttpServlet {
	private static final CsrfSecurityHandler DEFAULT_CSRF_SECURITY_HANDLER = new CsrfSecurityHandler();
	private static final UserManager         DEFAULT_USER_MANAGER          = new UserManager() {

		@Override
		public void setSessionUser(User user, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			super.setSessionUser(user, req, resp);
			String csrfToken = DEFAULT_CSRF_SECURITY_HANDLER.generateCsrfToken();
			DEFAULT_CSRF_SECURITY_HANDLER.setSessionCsrfToken(csrfToken, req, resp);
			DEFAULT_CSRF_SECURITY_HANDLER.sendCsrfToken(csrfToken, req, resp);
		}
		
	};
	
	private class CallerAction extends AbstractAction {
		private final Method method;
		
		public CallerAction(Method method) {
			super(null);
			this.method = method;
		}
		
		public CallerAction(Method method, SecurityHandler securityHandler) {
			super(securityHandler);
			this.method = method;
		}
		
		@Override
		protected void onProcessRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			try {
				method.invoke(ActionServlet.this, req, resp);
			} catch (InvocationTargetException ex) {
				Throwable cause = ex.getCause();
				
				if (cause instanceof IOException)
					throw (IOException) cause;
				
				if (cause instanceof ServletException)
					throw (ServletException) cause;
				
				throw new RuntimeException(cause);
			} catch (IllegalAccessException | IllegalArgumentException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	private final ActionDispatcher dispatcher = new ActionDispatcher();
	private Action beforeAction;
	private Action afterAction;
	private Action notFoundAction;
	
	private volatile boolean initialized = false;
	
	private synchronized void initialize() {
		Method[] methods = this.getClass().getMethods();
		for (Method method : methods) {
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof WebAction) {
					WebAction webAction = (WebAction) annotation;
					
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
					CallerAction callerAction = new CallerAction(method, handler);
					
					dispatcher.registerAction(callerAction, httpMethod, url);
				} else if (annotation instanceof BeforeAction) {
					if (beforeAction != null)
						throw new RuntimeException("Duplicate BeforeAction: " + method.getName());
					
					CallerAction callerAction = new CallerAction(method);
					beforeAction = callerAction;
				} else if (annotation instanceof AfterAction) {
					if (afterAction != null)
						throw new RuntimeException("Duplicate AfterAction: " + method.getName());
					
					CallerAction callerAction = new CallerAction(method);
					afterAction = callerAction;
				} else if (annotation instanceof NotFoundAction) {
					if (notFoundAction != null)
						throw new RuntimeException("Duplicate NotFoundAction: " + method.getName());
					
					CallerAction callerAction = new CallerAction(method);
					notFoundAction = callerAction;
				}
			}
		}
		initialized = true;
	}
	
	/**
	 * Returns the user manager used by this servlet
	 * @return the user manager used by this servlet
	 */
	protected UserManager getUserManager() {
		return DEFAULT_USER_MANAGER;
	}
	
	/**
	 * Returns the security manager used by this servlet
	 * @param requiredRoles required roles
	 * @return the security manager used by this servlet
	 */
	protected SecurityHandler getSecurityHandler(Set<String> requiredRoles) {
		Set<SecurityHandler> handlerSet = new LinkedHashSet<>();
		handlerSet.add(new UserRoleSecurityHandler(getUserManager(), requiredRoles));
		handlerSet.add(DEFAULT_CSRF_SECURITY_HANDLER);
		return new SecurityHandlerSet(handlerSet);
	}
	
	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!initialized)
			initialize();
		
		Action action = dispatcher.getAction(req);
		if (action == null) {
			if (notFoundAction != null) {
				notFoundAction.processRequest(req, resp);
			} else {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} else {
			if (beforeAction != null)
				beforeAction.processRequest(req, resp);

			action.processRequest(req, resp);

			if (afterAction != null)
				afterAction.processRequest(req, resp);
		}
	}
	// =========================================================================
}
