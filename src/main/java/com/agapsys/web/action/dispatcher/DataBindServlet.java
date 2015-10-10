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
 * Custom {@linkplain ActionServlet} to handle {@linkplain DataBindRequest} methods.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class DataBindServlet extends ActionServlet {
	private final LazyInitializer<DataBindController> dataBindController = new LazyInitializer<DataBindController>() {

		@Override
		protected DataBindController getLazyInstance() {
			return DataBindServlet.this.getController();
		}
	};
	
	@Override
	protected ActionCaller getActionCaller(Method method, SecurityHandler securityHandler) {
		return dataBindController.getInstance().getActionCaller(method, securityHandler);
	}
	
	/**
	 * Return the controller associated with this servlet.
	 * It is safe to return a new instance, since this method will be called only once during application execution.
	 * @return the controller associated with this servlet.
	 */
	protected abstract DataBindController getController();
	
	/** 
	 * Return the object sent from client (contained in the request)
	 * @return the object sent from client (contained in the request)
	 * @param exchange HTTP exchange
	 */
	public Object readObject(HttpExchange exchange) {
		return dataBindController.getInstance().readObject(exchange);
	}
	
	/**
	 * Sends given object to the client (contained in the response).
	 * @param exchange HTTP exchange
	 * @param obj object to be sent
	 */
	public void writeObject(HttpExchange exchange, Object obj) {
		dataBindController.getInstance().writeObject(exchange, obj);
	}
}
