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
	/** Custom action caller to handle {@linkplain DataBindRequest} methods. */
	static class DataBindMethodCallerAction extends MethodCallerAction {
		private final Class targetClass;
		private final ObjectSerializer serializer;
		private final boolean throwIfNonEntityEnclosed;

		public DataBindMethodCallerAction(
			ObjectSerializer serializer,
			Class targetClass,
			boolean throwIfNonEntityEnclosed,
			ActionService actionService, 
			Method method, 
			SecurityManager securityManager
		) {
			super(actionService, method, securityManager);
			this.targetClass = targetClass;
			this.serializer = serializer;
			this.throwIfNonEntityEnclosed = throwIfNonEntityEnclosed;
		}

		private boolean isEntityEnclosed(HttpExchange exchange) {
			String[] entityEnclosedMethods = {"POST", "PUT", "PATCH"};
			String method = exchange.getRequest().getMethod();
			
			for (String accepted : entityEnclosedMethods) {
				if (method.equalsIgnoreCase(accepted)) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		protected void onProcessRequest(HttpExchange exchange) {
			if (targetClass != null) {
				boolean isEntityEnclosed = isEntityEnclosed(exchange);
				if (throwIfNonEntityEnclosed && !isEntityEnclosed)
					throw new RuntimeException(String.format("Expecting %s for a non-entity-enclosed request (%s)", targetClass.getName(), exchange.getRequest().getMethod()));
				
				if (isEntityEnclosed) {
					try {
						Object targetObject = serializer.readObject(exchange, targetClass);
						exchange.getRequest().setAttribute(DataBindController.ATTR_TARGET_OBJECT, targetObject);
						super.onProcessRequest(exchange);
					} catch (ObjectSerializer.BadRequestException ex) {
						exchange.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST); // Skip request if target object cannot be obtained from request
					}
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
