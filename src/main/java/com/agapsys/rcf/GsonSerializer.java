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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GsonSerializer implements JsonSerializer {

	// CLASS SCOPE =============================================================
	public static final String JSON_CONTENT_TYPE = "application/json";
	public static final String JSON_ENCODING = "UTF-8";

	private static final Gson DEFAULT_GSON;
	
	static {
		GsonBuilder builder = new GsonBuilder();
		IsoDateAdapter adapter = new IsoDateAdapter();
		builder.registerTypeAdapter(Date.class, adapter);
		builder.registerTypeAdapter(Time.class, adapter);
		builder.registerTypeAdapter(java.sql.Date.class, adapter);
		builder.registerTypeAdapter(Timestamp.class, adapter);
		DEFAULT_GSON = builder.create();
	}

	// Check if given request is valid for GSON parsing
	private static void checkJsonContentType(HttpServletRequest req) throws BadRequestException {
		String reqContentType = req.getContentType();

		if (!reqContentType.startsWith(JSON_CONTENT_TYPE)) {
			throw new BadRequestException("Invalid content-type: " + reqContentType);
		}
	}

	private static class ListType implements ParameterizedType {

		private final Type[] typeArguments = new Type[1];

		public ListType(Class<?> clazz) {
			typeArguments[0] = clazz;
		}

		@Override
		public String getTypeName() {
			return String.format("java.util.List<%s>", typeArguments[0].getTypeName());
		}

		@Override
		public Type[] getActualTypeArguments() {
			return typeArguments;
		}

		@Override
		public Type getRawType() {
			return List.class;
		}

		@Override
		public Type getOwnerType() {
			return List.class;
		}
	}

	private static class IsoDateAdapter implements com.google.gson.JsonSerializer<Date>, JsonDeserializer<Date> {

		private final SimpleDateFormat sdf;

		public IsoDateAdapter() {
			this.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			this.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(sdf.format(src));
		}

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (!(json instanceof JsonPrimitive)) {
				throw new JsonParseException("Invalid date");
			}
			
			try {
				return sdf.parse(json.getAsString());
			} catch (ParseException ex) {
				throw new JsonSyntaxException(ex);
			}
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private Gson gson = null;

	private synchronized Gson _getGson() {
		if (gson == null) {
			gson = getGson();
		}

		return gson;
	}

	protected Gson getGson() {
		return DEFAULT_GSON;
	}

	@Override
	public <T> T readObject(String json, Class<T> targetClass) {
		return _getGson().fromJson(json, targetClass);
	}

	@Override
	public <T> T readObject(Reader json, Class<T> targetClass) {
		return _getGson().fromJson(json, targetClass);
	}

	@Override
	public <T> T readObject(InputStream json, String charset, Class<T> targetClass) {
		try {
			Reader reader = new InputStreamReader(json, charset);
			return readObject(reader, targetClass);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public <T> List<T> getJsonList(String json, Class<T> elementType) {
		return _getGson().fromJson(json, new ListType(elementType));
	}

	@Override
	public <T> List<T> getJsonList(Reader json, Class<T> elementType) {
		return _getGson().fromJson(json, new ListType(elementType));
	}

	@Override
	public <T> List<T> getJsonList(InputStream json, String charset, Class<T> elementType) {
		try {
			Reader reader = new InputStreamReader(json, charset);
			return getJsonList(reader, elementType);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String toJson(Object obj) {
		return _getGson().toJson(obj);
	}

	@Override
	public <T> T readObject(HttpServletRequest req, Class<T> targetClass) throws BadRequestException {
		if (targetClass == null) {
			throw new IllegalArgumentException("Null targetClass");
		}

		checkJsonContentType(req);

		try {
			return readObject(req.getReader(), targetClass);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed JSON");
		} catch (Throwable t) {
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				throw new RuntimeException(t);
			}
		}
	}

	@Override
	public void writeObject(HttpServletResponse resp, Object object) {
		resp.setContentType(JSON_CONTENT_TYPE);
		resp.setCharacterEncoding(JSON_ENCODING);

		PrintWriter out;
		try {
			out = resp.getWriter();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		String json = _getGson().toJson(object);
		out.write(json);
	}


	@Override
	public <T> List<T> getJsonList(HttpServletRequest req, Class<T> elementType) throws BadRequestException {
		checkJsonContentType(req);

		try {
			return getJsonList(req.getReader(), elementType);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed JSON");
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	// =========================================================================
}
