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

/**
 * Serializer/Deserializer of objects in HTTP communication
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public interface ObjectSerializer {
	// CLASS SCOPE =============================================================
	/** Exception thrown when a request does does not contain expected data. */
	public static class BadRequestException extends Exception {

		public BadRequestException() {
		}

		public BadRequestException(String message) {
			super(message);
		}

		public BadRequestException(String message, Throwable cause) {
			super(message, cause);
		}

		public BadRequestException(Throwable cause) {
			super(cause);
		}

		public BadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	/**
	 * Return an object sent from client (contained in the request).
	 * @param <T> Type of the returned object
	 * @param exchange HTTP exchange
	 * @param targetClass class of returned object
	 * @param params extra parameters passed to deserialization.
	 * @return returned object
	 * @throws BadRequestException if it was not possible to retrieve an object instance from given request
	 */
	public <T> T readObject(HttpExchange exchange, Class<T> targetClass, Object...params) throws BadRequestException;
	
	/**
	 * Sends given object to the client (contained in the response).
	 * @param exchange HTTP exchange
	 * @param object object to be sent
	 * @param params extra parameters passed to serialization.
	 */
	public void writeObject(HttpExchange exchange, Object object, Object...params);
	// =========================================================================
}
