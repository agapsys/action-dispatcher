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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Servlet responsible by mapping methods to actions
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class Controller extends ActionServlet {
	// CLASS SCOPE =============================================================

	/**
	 * Checks if an annotated method signature matches with required one.
	 *
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
						throw new RuntimeException(String.format("Invalid signature (%s). Required: public void <method_name>(%s)", method.toGenericString(), HttpExchange.class.getName()));
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

						MethodCallerAction action = new MethodCallerAction(this, method);

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
	// =========================================================================
}
