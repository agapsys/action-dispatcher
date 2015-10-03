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
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller to be used by {@linkplain ObjectRequestServlet} and/or {@linkplain JpaObjectRequestServlet}.
 * Since both servlet classes handles common code, in order to avoid code duplication, a common controller
 * handles all the similarities among classes
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class ObjectRequestController {
	// CLASS SCOPE =============================================================
	public static final String ATTR_TARGET_OBJECT = "com.agapsys.angular.demo.targetObject";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final ActionServlet servlet;
	private final LazyInitializer<ObjectSerializer> serializerLazyInitializer = new LazyInitializer<ObjectSerializer>() {

		@Override
		protected ObjectSerializer getLazyInstance() {
			return ObjectRequestController.this.getSerializer();
		}
	};	

	/**
	 * Constructor
	 * @param servlet an action servlet
	 */
	public ObjectRequestController(ActionServlet servlet) {
		if (servlet == null)
			throw new IllegalArgumentException("Null servlet");
		
		this.servlet = servlet;
	}
	
	/**
	 * Returns the serializer used by this controller. 
	 * It is safe to return a new instance, since this method will be called only once during application execution.
	 * @return the serializer used by this controller. 
	 */
	protected abstract ObjectSerializer getSerializer();
	
	/**
	 * Gets the action caller associated with a method
	 * @param method method
	 * @param securityHandler associated security handler
	 * @return action caller
	 */
	public final ActionCaller getActionCaller(Method method, SecurityHandler securityHandler) {
		ObjectRequest[] objectRequestAnnotations = method.getAnnotationsByType(ObjectRequest.class);
		ObjectRequest objectRequestAnnotation = objectRequestAnnotations.length > 0 ? objectRequestAnnotations[0] : null;
		
		Class targetClass = null;
		
		if (objectRequestAnnotation != null) {
			targetClass = objectRequestAnnotation.targetClass();
		}
		return new ObjectRequestActionCaller(serializerLazyInitializer.getInstance(), targetClass, servlet, method, securityHandler);
	}
	
	/**
	 * @return the instance of a class specified in {@linkplain ObjectRequest}.
	 * @param req HTTP request
	 */
	public final Object getObject(HttpServletRequest req) {
		return req.getAttribute(ATTR_TARGET_OBJECT);
	}
	
	/**
	 * Sends an object to the client
	 * @param resp HTTP response
	 * @param obj object to be sent
	 * @throws IOException if there is an error while sending the response
	 */
	public final void sendObject(HttpServletResponse resp, Object obj) throws IOException {
		serializerLazyInitializer.getInstance().sendObject(resp, obj);
	}
	// =========================================================================
}
