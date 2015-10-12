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
import javax.servlet.http.HttpServletResponse;

/**
 * Custom {@linkplain ActionServlet} to handle {@linkplain DataBindRequest} methods.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class DataBindServlet extends ActionServlet implements DataBindService {
	// CLASS SCOPE =============================================================
	private static final String ATTR_TARGET_OBJECT = "com.agapsys.angular.demo.targetObject";
	
	/** Data bind controller used by a {@link DataBindService}. */
	public static abstract class DataBindController {
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

		// CUSTOMIZABLE INITIALIZATION BEHAVIOUR -------------------------------
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

			if (dataBindRequestAnnotation != null) {
				targetClass = dataBindRequestAnnotation.targetClass();
			}
			return new DataBindMethodCallerAction(objectSerializer.getInstance(), targetClass, dataBindService, method, securityManager);
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

	/** Custom action caller to handle {@linkplain DataBindRequest} methods. */
	private static class DataBindMethodCallerAction extends MethodCallerAction {
		private final Class targetClass;
		private final ObjectSerializer serializer;

		/**
		 * Constructor.
		 * @param serializer object serializer
		 * @param targetClass target class
		 * @param actionService action service
		 * @param method mapped method
		 * @param securityManager security manager
		 */
		public DataBindMethodCallerAction(
			ObjectSerializer serializer,
			Class targetClass, 
			ActionService actionService, 
			Method method, 
			SecurityManager securityManager
		) {
			super(actionService, method, securityManager);
			this.targetClass = targetClass;
			this.serializer = serializer;
		}

		@Override
		protected void onProcessRequest(HttpExchange exchange) {
			if (targetClass != null) {
				try {
					Object targetObject = serializer.readObject(exchange, targetClass);
					exchange.getRequest().setAttribute(ATTR_TARGET_OBJECT, targetObject);
					super.onProcessRequest(exchange);
				} catch (ObjectSerializer.BadRequestException ex) {
					exchange.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST); // Skip request if target object cannot be obtained from request
				}
			} else {
				super.onProcessRequest(exchange);
			}
		}
	}
	// =========================================================================
	
	private final LazyInitializer<DataBindController> dataBindController = new LazyInitializer<DataBindController>() {

		@Override
		protected DataBindController getLazyInstance(Object...params) {
			return DataBindServlet.this._getController();
		}
	};
	
	// CUSTOMIZABLE INITIALIZATION BEHAVIOUR -----------------------------------
	@Override
	protected MethodCallerAction _getMethodCallerAction(Method method, SecurityManager securityManager) {
		return dataBindController.getInstance()._getMethodCallerAction(method, securityManager);
	}
	
	/**
	 * Return the controller associated with this servlet.
	 * It is safe to return a new instance, since this method will be called only once during application execution.
	 * @return the controller associated with this servlet.
	 */
	protected abstract DataBindController _getController();
	// -------------------------------------------------------------------------
	
	@Override
	public Object readObject(HttpExchange exchange) {
		return dataBindController.getInstance().readObject(exchange);
	}
	
	@Override
	public void writeObject(HttpExchange exchange, Object obj) {
		dataBindController.getInstance().writeObject(exchange, obj);
	}
}
