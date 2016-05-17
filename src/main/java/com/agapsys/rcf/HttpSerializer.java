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
import com.agapsys.rcf.exceptions.CheckedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serializer/Deserializer of objects in HTTP communication
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class HttpSerializer {
	// STATIC SCOPE ============================================================
	/**
	 * Checks if given request contains expected content-type.
	 * @param req HTTP request
	 * @param contentType expected content-type
	 * @throws BadRequestException if given request is not valid.
	 */
	static void checkContentType(HttpServletRequest req, String contentType) throws BadRequestException {
		String reqContentType = req.getContentType();

		if (!reqContentType.startsWith(contentType)) {
			throw new BadRequestException("Invalid content-type: " + reqContentType);
		}
	}
	
	public static class SerializerException extends CheckedException {

		public SerializerException() {}

		public SerializerException(String msg, Object... msgArgs) {
			super(msg, msgArgs);
		}

		public SerializerException(Throwable throwable) {
			super(throwable);
		}

		public SerializerException(Throwable throwable, String msg, Object... msgArgs) {
			super(throwable, msg, msgArgs);
		}
		
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	/**
	 * Returns the content-type used by this serializer.
	 * @return the content-type used by this serializer.
	 */
	public abstract String getContentType();
	
	/**
	 * Returns the charset used by this serializer.
	 * @return the charset used by this serializer.
	 */
	public abstract String getCharset();
	
	/**
	 * Return an object sent from client (contained in the request).
	 * @param <T> Type of the returned object.
	 * @param req HTTP request.
	 * @param targetClass class of returned object.
	 * @return an instance of given target class.
	 * @throws BadRequestException if it was not possible to retrieve an object instance from given request.
	 * @throws IOException if an error happened during the operation.
	 */
	public final <T> T readObject(HttpServletRequest req, Class<T> targetClass) throws BadRequestException, IOException {
		checkContentType(req, getContentType());
		
		if (targetClass == null) throw new IllegalArgumentException("Null targetClass");
		
		try {
			return readObject(req.getInputStream(), getCharset(), targetClass);
		} catch (SerializerException ex) {
			throw new BadRequestException(ex.getMessage());
		}
	}
	
	/**
	 * @param <T> Type of the returned object
	 * @param inputStream input stream
	 * @param targetClass class of returned object
	 * @param charset charset
	 * @return an instance of given target class
	 * @throws SerializerException if it was not possible to retrieve an object instance from given input stream
	 * @throws IOException if an error happened during the operation
	 */
	public abstract <T> T readObject(InputStream inputStream, String charset, Class<T> targetClass) throws SerializerException, IOException;
	
	/**
	 * Sends given object to the client (contained in the response).
	 * @param resp HTTP response
	 * @param object object to be sent
	 * @throws IOException if an error happened during the operation
	 */
	public final void writeObject(HttpServletResponse resp, Object object) throws IOException {
		resp.setContentType(getContentType());
		resp.setCharacterEncoding(getCharset());

		PrintWriter out = resp.getWriter();
		out.write(toString(object));
	}
	
	/**
	 * Returns the string representation of an object.
	 * @param obj object to be converted.
	 * @return the string representation of given object.
	 */
	public abstract String toString(Object obj);
	// =========================================================================
}
