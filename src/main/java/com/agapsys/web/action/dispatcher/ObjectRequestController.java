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

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;

/**
 * Controller to be used by {@linkplain ObjectRequestServlet} and/or {@linkplain JpaObjectRequestServlet}.
 * Since both servlet classes handles common code, in order to avoid code duplication, a common controller
 * handles all the similarities among classes
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ObjectRequestController {
	// CLASS SCOPE =============================================================
	public static final String ATTR_TARGET_OBJECT = "com.agapsys.angular.demo.targetObject";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final ActionServlet servlet;
	private final ObjectSerializer serializer;
	
	/**
	 * Constructor
	 * @param servlet an action servlet
	 * @param serializer object serializer / deserializer
	 */
	public ObjectRequestController(ActionServlet servlet, ObjectSerializer serializer) {
		if (servlet == null)
			throw new IllegalArgumentException("Null servlet");
		
		if (serializer == null)
			throw new IllegalArgumentException("Null serializer");
		
		this.servlet = servlet;
		this.serializer = serializer;
	}
	
	/**
	 * Gets the action caller associated with a method
	 * @param method method
	 * @param securityHandler associated security handler
	 * @return action caller
	 */
	public ActionCaller getActionCaller(Method method, SecurityHandler securityHandler) {
		ObjectRequest[] objectRequestAnnotations = method.getAnnotationsByType(ObjectRequest.class);
		ObjectRequest objectRequestAnnotation = objectRequestAnnotations.length > 0 ? objectRequestAnnotations[0] : null;
		
		Class targetClass = null;
		
		if (objectRequestAnnotation != null) {
			targetClass = objectRequestAnnotation.targetClass();
		}
		return new ObjectRequestActionCaller(serializer, targetClass, servlet, method, securityHandler);
	}
	
	/** @return the instance of a class specified in {@linkplain ObjectRequest}. */
	public Object getObject(HttpServletRequest req) {
		return req.getAttribute(ATTR_TARGET_OBJECT);
	}
	// =========================================================================
}
