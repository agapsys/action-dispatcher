/*
 * Copyright 2017 Agapsys Tecnologia Ltda-ME.
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
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsonRequest extends ActionRequest {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String JSON_ENCODING     = "UTF-8";

    static final Gson DEFAULT_GSON;

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

    private static class IsoDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

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

    public static <T> T readObject(Reader reader, Class<T> targetClass) {
        return DEFAULT_GSON.fromJson(reader, targetClass);
    }

    public static <E> List<E> readList(Reader reader, Class<E> elementClass) {
        return DEFAULT_GSON.fromJson(reader, new ListType(elementClass));
    }
    // =========================================================================
    // </editor-fold>

    public JsonRequest(ActionRequest wrappedRequest) {
        super(wrappedRequest);
    }

    public JsonRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        super(servletRequest, servletResponse);
    }
    

    /**
     * Reads an object represented by JSON request.
     *
     * @param <T> object type.
     * @param targetClass object class.
     * @return an object contained in the request.
     * @throws IOException if an I/O error happened during the process.
     * @throws BadRequestException if data contained in the request does not represent an instance of given class.
     */
    public final <T> T readObject(Class<T> targetClass) throws IOException, BadRequestException {
        String reqContentType = getServletRequest().getContentType();

        if (reqContentType == null || !reqContentType.startsWith(JSON_CONTENT_TYPE))
            throw new BadRequestException("Invalid content-type: " + reqContentType);

        if (targetClass == null)
            throw new IllegalArgumentException("Null targetClass");

        try {
            Reader reader = new InputStreamReader(getServletRequest().getInputStream(), JSON_ENCODING);
            return readObject(reader, targetClass);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        } catch (JsonSyntaxException ex) {
            throw new BadRequestException("Invalid data");
        }
    }

    /**
     * Reads a list of objects contained in request.
     *
     * @param <E> element type.
     * @param elementClass element class.
     * @return a list of object contained in the request.
     * @throws IOException if an I/O error happened during the process.
     * @throws BadRequestException if data contained in the request does not represent a list of objects.
     */
    public final <E> List<E> readList(Class<E> elementClass) throws IOException, BadRequestException {
        try {
            Reader reader = new InputStreamReader(getServletRequest().getInputStream(), JSON_ENCODING);
            return readList(reader, elementClass);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        } catch (JsonSyntaxException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}
