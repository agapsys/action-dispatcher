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

package com.agapsys.rcf;

import com.agapsys.rcf.exceptions.BadRequestException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serializer/Deserializer of objects in HTTP communication
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public interface ObjectSerializer {
	
	/**
	 * Return an object sent from client (contained in the request).
	 * @param <T> Type of the returned object
	 * @param req HTTP request
	 * @param targetClass class of returned object
	 * @return returned object
	 * @throws BadRequestException if it was not possible to retrieve an object instance from given request
	 */
	public <T> T readObject(HttpServletRequest req, Class<T> targetClass) throws BadRequestException;
	
	/**
	 * Sends given object to the client (contained in the response).
	 * @param resp HTTP response
	 * @param object object to be sent
	 */
	public void writeObject(HttpServletResponse resp, Object object);
}
