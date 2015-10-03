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
 * Custom {@linkplain ActionServlet} to handle {@linkplain ObjectRequest} methods.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class ObjectRequestServlet extends ActionServlet {
	
	@Override
	protected ActionCaller getActionCaller(Method method, SecurityHandler securityHandler) {
		return getController().getActionCaller(method, securityHandler);
	}
	
	/**
	 * Return the controller associated with this servlet.
	 * @return the controller associated with this servlet.
	 */
	protected abstract ObjectRequestController getController();
	
	/** @return the instance of a class specified in {@linkplain ObjectRequest}. */
	public Object getObject(HttpServletRequest req) {
		return getController().getObject(req);
	}
}
