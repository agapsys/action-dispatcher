/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public abstract class JsonHttpSerializer extends HttpObjectSerializer {
	// STATIC SCOPE ============================================================
	private static final String JSON_CONTENT_TYPE = "application/json";
	private static final String JSON_ENCODING = "UTF-8";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	public abstract <T> List<T> getJsonList(InputStream json, String charset, Class<T> elementType) throws IOException, SerializerException;

	public final String toJson(Object obj) {
		return toString(obj);
	}
	
	@Override
	public String getContentType() {
		return JSON_CONTENT_TYPE;
	}

	@Override
	public String getCharset() {
		return JSON_ENCODING;
	}

	/**
	 * Returns a list of objects from given request.
	 *
	 * @param <T> Type of list elements.
	 * @param req HTTP request
	 * @param elementClass class of list elements
	 * @return list stored in request content body
	 * @throws BadRequestException if it was not possible to retrieve a list of objects from given request.
	 * @throws IOException if an error happened during the operation
	 */
	public final <T> List<T> getJsonList(HttpServletRequest req, Class<T> elementClass) throws BadRequestException, IOException {
		HttpObjectSerializer.checkContentType(req, getContentType());

		try {
			return getJsonList(req.getInputStream(), getCharset(), elementClass);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed JSON");
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	// =========================================================================
}
