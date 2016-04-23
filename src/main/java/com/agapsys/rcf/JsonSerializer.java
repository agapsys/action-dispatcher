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
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface JsonSerializer extends ObjectSerializer {
	
	public <T> T readObject(String json, Class<T> targetClass);

	public <T> T readObject(Reader json, Class<T> targetClass);

	public <T> T readObject(InputStream json, String charset, Class<T> targetClass);

	public <T> List<T> getJsonList(String json, Class<T> elementType);

	public <T> List<T> getJsonList(Reader json, Class<T> elementType);

	public <T> List<T> getJsonList(InputStream json, String charset, Class<T> elementType);

	public String toJson(Object obj);
	
	/**
	 * Returns a list of objects from given request Request must have
	 * 'application/json' content-type.
	 *
	 * @param <T> generic type
	 * @param req HTTP request
	 * @param elementType type of the list elements
	 * @return list stored in request content body
	 * @throws BadRequestException if given request content-type does not match
	 * with expected
	 */
	public <T> List<T> getJsonList(HttpServletRequest req, Class<T> elementType) throws BadRequestException;
	
}
