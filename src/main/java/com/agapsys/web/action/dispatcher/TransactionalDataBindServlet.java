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

import com.agapsys.web.action.dispatcher.DataBindServlet.DataBindController;
import java.lang.reflect.Method;

/**
 * Custom {@link TransactionalServlet} to handle {@linkplain DataBindRequest} methods
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class TransactionalDataBindServlet extends TransactionalServlet implements DataBindService {
	private final LazyInitializer<DataBindController> dataBindController = new LazyInitializer<DataBindController>() {

		@Override
		protected DataBindController getLazyInstance(Object...params) {
			return TransactionalDataBindServlet.this._getController();
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