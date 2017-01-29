/*
 * Copyright 2016-2017 Agapsys Tecnologia Ltda-ME.
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
import com.agapsys.rcf.exceptions.MethodNotAllowedException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ActionRequest extends ServletExchange {
    
    private static interface ParamConverter<T> {
        public T getParam(String strVal) throws BadRequestException;
    }
    
    private static abstract class AbstractParamConverter<T> implements ParamConverter<T> {
        
        private final boolean trim;
        
        public AbstractParamConverter(boolean trim) {
            this.trim = trim;
        }
        
        public AbstractParamConverter() {
            this(true);
        }
        
        @Override
        public final T getParam(String strVal) throws BadRequestException {
            if (strVal == null)
                return null;
            
            strVal = trim ? strVal.trim() : strVal;
            try {
                return _getParam(strVal);
            } catch (RuntimeException ex) {
                throw new BadRequestException(ex.getMessage());
            }
        }
        
        protected abstract T _getParam(String strVal) throws BadRequestException;
        
    }
    
    private static final Map<Class, ParamConverter> PARAM_CONVERTER_MAP = new LinkedHashMap<>();
    
    static {
         final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        PARAM_CONVERTER_MAP.put(Byte.class,       new AbstractParamConverter<Byte>() {
            @Override
            public Byte _getParam(String strVal) throws BadRequestException {
                return Byte.parseByte(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Short.class,      new AbstractParamConverter<Short>() {
            @Override
            protected Short _getParam(String strVal) throws BadRequestException {
                return Short.parseShort(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Integer.class,    new AbstractParamConverter<Integer>() {
            @Override
            protected Integer _getParam(String strVal) throws BadRequestException {
                return Integer.parseInt(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Long.class,       new AbstractParamConverter<Long>() {
            @Override
            protected Long _getParam(String strVal) throws BadRequestException {
                return Long.parseLong(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Float.class,      new AbstractParamConverter<Float>() {
            @Override
            protected Float _getParam(String strVal) throws BadRequestException {
                return Float.parseFloat(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Double.class,     new AbstractParamConverter<Double>() {
            @Override
            protected Double _getParam(String strVal) throws BadRequestException {
                return Double.parseDouble(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(BigDecimal.class, new AbstractParamConverter<BigDecimal>() {
            @Override
            protected BigDecimal _getParam(String strVal) throws BadRequestException {
                return new BigDecimal(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Date.class,       new AbstractParamConverter<Date>() {
            
            
            @Override
            protected Date _getParam(String strVal) throws BadRequestException {
                try {
                    return sdf.parse(strVal);
                } catch (ParseException ex) {
                    throw new BadRequestException(ex.getMessage());
                }
            }
        });
        PARAM_CONVERTER_MAP.put(String.class,     new AbstractParamConverter<String>(false) {
            @Override
            protected String _getParam(String strVal) throws BadRequestException {
                return strVal;
            }
        });
        
    }

    static String _getRelativePath(String parent, String child) {
        if (parent.endsWith("/"))
            parent = parent.substring(0, parent.length() - 1);

        if (child.endsWith("/"))
            child = child.substring(0, child.length() - 1);

        String tmpPath = child.replaceFirst(Pattern.quote(parent), "");
        return tmpPath.startsWith("/") ? tmpPath : "/" + tmpPath;
    }

    private final ActionRequest       wrappedRequest;
    private final HttpMethod          method;
    private final String              requestUri;
    private final String              pathInfo;
    private final Map<String, String> paramMap;

    // TODO make test
    public static void main(String[] args) {
        String child = "/foo/path/to/resource";
        String parent = "/foo/path";
        String relativePath = _getRelativePath(parent, child);

        child = "/foo/path";
        parent = "/bar/path";
        relativePath = _getRelativePath(parent, child);

        child = "/abc";
        parent = "/";
        relativePath = _getRelativePath(parent, child);

        child = "/";
        parent = "/";
        relativePath = _getRelativePath(parent, child);

        child = "/abc/";
        parent = "/abc";
        relativePath = _getRelativePath(parent, child);

        child = "/abc";
        parent = "/abc/";
        relativePath = _getRelativePath(parent, child);

        return;
    }

    // Generic constructor
    ActionRequest(String parentPath, ActionRequest wrappedRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws MethodNotAllowedException {
        super(servletRequest, servletResponse);
        this.wrappedRequest = wrappedRequest;

        try {
            this.method = HttpMethod.valueOf(servletRequest.getMethod());
        } catch (IllegalArgumentException ex) {
            throw new MethodNotAllowedException();
        }

        requestUri = servletRequest.getRequestURI();

        if (wrappedRequest == null || parentPath == null) {
            String pathInfo = servletRequest.getPathInfo();
            this.pathInfo = pathInfo == null ? "/" : pathInfo;
            Map<String, String> tmpParameters = new LinkedHashMap<>();
            for (Map.Entry<String, String[]> entry : servletRequest.getParameterMap().entrySet()) {
                String[] values = entry.getValue();
                tmpParameters.put(entry.getKey(), values[values.length - 1]);
            }
            paramMap = Collections.unmodifiableMap(tmpParameters);
        } else { // <-- wrappedRequest != null && parentPath != null
            pathInfo = _getRelativePath(parentPath, wrappedRequest.pathInfo);
            paramMap = wrappedRequest.paramMap;
        }
    }

    ActionRequest(String parentPath, ActionRequest wrappedRequest) {
        this(parentPath, wrappedRequest, wrappedRequest.getServletRequest(), wrappedRequest._getServletResponse());
    }

    ActionRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this(null, null, servletRequest, servletResponse);
    }

    protected ActionRequest(ActionRequest wrappedRequest) {
        this(null, wrappedRequest, wrappedRequest.getServletRequest(), wrappedRequest._getServletResponse());
    }

    /**
     * Returns the HTTP method associated with this request.
     *
     * @return the HTTP method associated with this request.
     */
    public final HttpMethod getMethod() {
        return method;
    }

    protected final ActionRequest getWrappedRequest() {
        return wrappedRequest;
    }

    public final String getRequestUri() {
        return requestUri;
    }

    public final String getPathInfo() {
        return pathInfo;
    }

    /**
     * Return origin IP.
     *
     * @return origin IP.
     */
    public final String getOriginIp() {
        return getServletRequest().getRemoteAddr();
    }

    /**
     * Return origin user-agent.
     *
     * @return origin user-agent.
     */
    public final String getUserAgent() {
        return getServletRequest().getHeader("user-agent");
    }

    /**
     * Return cookie value.
     *
     * @return cookie value. If there is no such cookie, returns null.
     * @param name cookie name
     */
    public final String getCookie(String name) {
        Cookie[] cookies = getServletRequest().getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Returns an optional parameter contained in the request.
     *
     * @param paramName parameter name
     * @param defaultValue default value if given parameter is not contained in the request
     * @return parameter value
     */
    public final String getOptionalParameter(String paramName, String defaultValue) {
        return getOptionalParameter(String.class, paramName, defaultValue);
    }
    
    public final <T> T getOptionalParameter(Class<T> targetClass, String paramName, T defaultValue) {
        String strVal = getServletRequest().getParameter(paramName);
        ParamConverter<T> converter = PARAM_CONVERTER_MAP.get(targetClass);
        
        if (converter == null)
            throw new UnsupportedOperationException("There is no converter for " + targetClass.getName());
        
        T t = converter.getParam(strVal);
        
        if (t == null)
            return defaultValue;
        
        return t;
    }

    /** @see HttpServletRequest#getHeader(java.lang.String) */
    public final String getHeader(String name) {
        return getServletRequest().getHeader(name);
    }

    /** @see HttpServletRequest#getHeaders(java.lang.String) */
    public final Enumeration<String> getHeaders(String name) {
        return getServletRequest().getHeaders(name);
    }

    /**
     * Return query string parameter map.
     *
     * @return query string parameter map.
     */
    public final Map<String, String> getParameterMap() {
        return paramMap;
    }

    /**
     * Returns a mandatory parameter contained in the request.
     *
     * @param paramName parameter name
     * @return parameter value.
     * @throws BadRequestException if parameter is not contained in given request.
     */
    public final String getMandatoryParameter(String paramName) throws BadRequestException {
        return getMandatoryParameter(paramName, "Missing parameter: %s", paramName);
    }

    public final <T> T getMandatoryParameter(Class<T> targetClass, String paramName) throws BadRequestException {
        return getMandatoryParameter(targetClass, paramName, "Missing parameter: %s", paramName);
    }
    
    /**
     * Returns a mandatory parameter contained in the request.
     *
     * @param paramName parameter name
     * @param errorMessage error message if parameter is not found.
     * @param errMsgArgs optional error message args if error message is a formatted string.
     * @return parameter value.
     * @throws BadRequestException if parameter is not contained in given request.
     */
    public final String getMandatoryParameter(String paramName, String errorMessage, Object...errMsgArgs) throws BadRequestException {
        return getMandatoryParameter(String.class, paramName, errorMessage, errMsgArgs);
    }

    public final <T> T getMandatoryParameter(Class<T> targetClass, String paramName, String errorMsg, Object...errMsgArgs) throws BadRequestException {
        String strVal = getServletRequest().getParameter(paramName);
        ParamConverter<T> converter = PARAM_CONVERTER_MAP.get(targetClass);
        
        if (converter == null)
            throw new UnsupportedOperationException("There is no converter for " + targetClass.getName());
        
        T t = converter.getParam(strVal);
        
        if (t == null)
            throw new BadRequestException(errorMsg, errMsgArgs);

        
        return t;
    }
    
    public final String getRequestUrl() {
        return getServletRequest().getRequestURL().toString();
    }

    public final String getFullRequestUrl() {
        HttpServletRequest req = getServletRequest();

        StringBuffer requestUrl = req.getRequestURL();
        if (req.getQueryString() != null)
            requestUrl.append("?").append(req.getQueryString());

        return requestUrl.toString();
    }

    public final String getQueryString() {
        return getServletRequest().getQueryString();
    }

    public final void putMetadata(String key, Object value) {
        getServletRequest().setAttribute(key, value);
    }

    public final Object getMetadata(String key) {
        return getServletRequest().getAttribute(key);
    }

    public final void removeMetadata(String key) {
        getServletRequest().removeAttribute(key);
    }

    public final String getProtocol() {
        return getServletRequest().getProtocol();
    }

    public final String getRequestLine() {
        HttpServletRequest req = getServletRequest();

        StringBuilder requestLine = new StringBuilder();
        requestLine.append(getMethod().name()).append(" ").append(getRequestUri());

        String queryString = req.getQueryString();
        if (queryString != null)
            requestLine.append("?").append(queryString);

        requestLine.append(" ").append(getProtocol());

        return requestLine.toString();
    }

    public final HttpServletRequest getServletRequest() {
        return _getServletRequest();
    }

    @Override
    public String toString() {
        return getRequestLine();
    }

}
