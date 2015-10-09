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
 * Custom {@linkplain ActionServlet} to handle {@linkplain ObjectRequest} methods.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class ObjectRequestServlet extends ActionServlet {
	private final LazyInitializer<ObjectRequestController> controllerLazyInitializer = new LazyInitializer<ObjectRequestController>() {

		@Override
		protected ObjectRequestController getLazyInstance() {
			return ObjectRequestServlet.this.getController();
		}
	};
	
	@Override
	protected ActionCaller getActionCaller(Method method, SecurityHandler securityHandler) {
		return controllerLazyInitializer.getInstance().getActionCaller(method, securityHandler);
	}
	
	/**
	 * Return the controller associated with this servlet.
	 * It is safe to return a new instance, since this method will be called only once during application execution.
	 * @return the controller associated with this servlet.
	 */
	protected abstract ObjectRequestController getController();
	
	/** 
	 * @return the instance of a class specified in {@linkplain ObjectRequest}.
	 * @param rrp request-response pair
	 */
	public Object getObject(RequestResponsePair rrp) {
		return controllerLazyInitializer.getInstance().getObject(rrp);
	}
	
	/**
	 * Sends an object to the client
	 * @param rrp request-response pair
	 * @param obj object to be sent
	 */
	public void sendObject(RequestResponsePair rrp, Object obj) {
		controllerLazyInitializer.getInstance().sendObject(rrp, obj);
	}
}
