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
 * Custom action caller to handle {@linkplain ObjectRequest} methods
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
class ObjectRequestActionCaller extends ActionCaller {
	private final Class targetClass;
	private final ObjectSerializer serializer;
	
	/**
	 * Constructor.
	 * @param serializer object serializer
	 * @param targetClass target class
	 * @param servlet action servlet
	 * @param method mapped method
	 * @param securityHandler security handler
	 */
	public ObjectRequestActionCaller(
		ObjectSerializer serializer,
		Class targetClass, 
		ActionServlet servlet, 
		Method method, 
		SecurityHandler securityHandler
	) {
		super(servlet, method, securityHandler);
		this.targetClass = targetClass;
		this.serializer = serializer;
	}

	@Override
	protected void onProcessRequest(RequestResponsePair rrp) {
		if (targetClass != null) {
			try {
				Object targetObject = serializer.getObject(rrp, targetClass);
				rrp.getRequest().setAttribute(ObjectRequestController.ATTR_TARGET_OBJECT, targetObject);
				super.onProcessRequest(rrp);
			} catch (ObjectSerializer.BadRequestException ex) {
				sendError(rrp, HttpServletResponse.SC_BAD_REQUEST); // Skip request if target object cannot be obtained from request
			}
		} else {
			super.onProcessRequest(rrp);
		}
	}
}
