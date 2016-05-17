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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class GsonHttpSerializer extends JsonHttpSerializer {

	// CLASS SCOPE =============================================================
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
	public <T> T readObject(InputStream json, String charset, Class<T> targetClass) throws SerializerException, IOException {

		if (targetClass == null) throw new IllegalArgumentException("Null targetClass");

		try {
			Reader reader = new InputStreamReader(json, charset);
			return _getGson().fromJson(reader, targetClass);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		} catch (JsonSyntaxException ex) {
			throw new SerializerException("Malformed JSON");
		} catch (JsonIOException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public <T> List<T> getJsonList(InputStream json, String charset, Class<T> elementType) throws IOException, SerializerException {
		try {
			Reader reader = new InputStreamReader(json, charset);
			return _getGson().fromJson(reader, new ListType(elementType));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		} catch (JsonSyntaxException ex) {
			throw new SerializerException("Malformed JSON");
		} catch (JsonIOException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public String toString(Object obj) {
		return _getGson().toJson(obj);
	}
	// =========================================================================
}
