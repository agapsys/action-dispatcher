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

/** 
 * Data bind controller used by a {@link DataBindService}.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class DataBindController {
	// CLASS SCOPE =============================================================
	static final String ATTR_TARGET_OBJECT = "com.agapsys.web.action.dispatcher.targetObject";

	private final DataBindService dataBindService;
	private final LazyInitializer<ObjectSerializer> objectSerializer = new LazyInitializer<ObjectSerializer>() {

		@Override
		protected ObjectSerializer getLazyInstance(Object...params) {
			return DataBindController.this._getSerializer();
		}
	};

	/**
	 * Constructor
	 * @param dataBindService data binding service.
	 */
	public DataBindController(DataBindService dataBindService) {
		if (dataBindService == null)
			throw new IllegalArgumentException("Null data binding service");

		this.dataBindService = dataBindService;
	}

	// CUSTOMIZABLE INITIALIZATION BEHAVIOUR -----------------------------------
	/**
	 * Returns the serializer used by this controller.
	 * This method is intended to be overridden to change object initialization and not be called directly
	 * @return the serializer used by this controller. 
	 */
	protected abstract ObjectSerializer _getSerializer();

	/**
	 * Gets the method caller action associated with given method.
	 * This method is intended to be overridden to change object initialization and not be called directly
	 * @param method method
	 * @param securityManager associated security manager
	 * @return method caller action associated with given method.
	 */
	MethodCallerAction _getMethodCallerAction(Method method, SecurityManager securityManager) {
		DataBindRequest[] dataBindRequestAnnotations = method.getAnnotationsByType(DataBindRequest.class);
		DataBindRequest dataBindRequestAnnotation = dataBindRequestAnnotations.length > 0 ? dataBindRequestAnnotations[0] : null;

		Class targetClass = null;
		HttpMethod[] ignoredMethods = null;
		
		if (dataBindRequestAnnotation != null) {
			targetClass = dataBindRequestAnnotation.targetClass();
			ignoredMethods = dataBindRequestAnnotation.ignoredMethods();			
		}
		
		return new DataBindServlet.DataBindMethodCallerAction(
			objectSerializer.getInstance(),
			targetClass,
			ignoredMethods,
			dataBindService, 
			method, 
			securityManager
		);
	}
	// -------------------------------------------------------------------------

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