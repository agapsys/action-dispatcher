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

import com.agapsys.web.action.dispatcher.ActionCaller.DataBindActionCaller;
import java.lang.reflect.Method;

/**
 * Controller to be used by {@linkplain DataBindServlet} and/or {@linkplain TransactionalDataBindServlet}.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class DataBindController {
	// CLASS SCOPE =============================================================
	public static final String ATTR_TARGET_OBJECT = "com.agapsys.angular.demo.targetObject";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final ActionServlet servlet;
	private final LazyInitializer<ObjectSerializer> objectSerializer = new LazyInitializer<ObjectSerializer>() {

		@Override
		protected ObjectSerializer getLazyInstance() {
			return DataBindController.this.getSerializer();
		}
	};	

	/**
	 * Constructor
	 * @param servlet an action servlet
	 */
	public DataBindController(ActionServlet servlet) {
		if (servlet == null)
			throw new IllegalArgumentException("Null servlet");
		
		if (!(servlet instanceof DataBindServlet) && !(servlet instanceof TransactionalDataBindServlet))
			throw new IllegalArgumentException(String.format("servlet is not an instance of %s nor %s", DataBindServlet.class.getName(), TransactionalDataBindServlet.class.getName()));
		
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
	public ActionCaller getActionCaller(Method method, SecurityHandler securityHandler) {
		DataBindRequest[] objectRequestAnnotations = method.getAnnotationsByType(DataBindRequest.class);
		DataBindRequest objectRequestAnnotation = objectRequestAnnotations.length > 0 ? objectRequestAnnotations[0] : null;
		
		Class targetClass = null;
		
		if (objectRequestAnnotation != null) {
			targetClass = objectRequestAnnotation.targetClass();
		}
		return new DataBindActionCaller(objectSerializer.getInstance(), targetClass, servlet, method, securityHandler);
	}
	
	/** 
	 * Return the object sent from client (contained in the request)
	 * @return the object sent from client (contained in the request)
	 * @param exchange HTTP exchange
	 */
	public final Object readObject(HttpExchange exchange) {
		return exchange.getRequest().getAttribute(ATTR_TARGET_OBJECT);
	}
	
	/**
	 * Sends given object to the client (contained in the response).
	 * @param exchange HTTP exchange
	 * @param obj object to be sent
	 */
	public final void writeObject(HttpExchange exchange, Object obj) {
		objectSerializer.getInstance().writeObject(exchange, obj);
	}
	// =========================================================================
}
