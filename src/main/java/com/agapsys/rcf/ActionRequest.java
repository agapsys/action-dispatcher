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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ActionRequest extends ServletExchange {
    
    //<editor-fold defaultstate="collapsed" desc="STATIC SCOPE">
    private static interface ParamConverter<T> {
        public T getParam(String strVal) throws BadRequestException;
    }
    
    private static <E> E __getEnum(Class<E> enumClass, String strVal) throws BadRequestException {
        if (strVal == null)
            return null;
        
        E[] enumValues = enumClass.getEnumConstants();
        
        if (enumValues != null) {
            for (E e : enumValues) {
                String name = ((Enum)e).name();
                if (strVal.equals(name))
                    return e;
            }
        }
        
        throw new BadRequestException("Invalid enum value: %s", strVal);
    }
    
    private static abstract class AbstractParamConverter<T> implements ParamConverter<T> {
        
        private final boolean trim;
        private final Class<T> targetClass;
        
        private AbstractParamConverter(Class<T> targetClass, boolean trim) {
            this.targetClass = targetClass;
            this.trim = trim;
        }
        
        private AbstractParamConverter(Class<T> targetClass) {
            this(targetClass, true);
        }
        
        @Override
        public final T getParam(String strVal) throws BadRequestException {
            if (strVal == null)
                return null;
            
            strVal = trim ? strVal.trim() : strVal;
            
            try {
                return _getParam(strVal);
            } catch (RuntimeException ex) {
                throw new BadRequestException("Cannot convert \"%s\" into %s", strVal, targetClass.getName());
            }
        }
        
        protected abstract T _getParam(String strVal) throws BadRequestException;
        
    }
    
    private static final Map<Class, ParamConverter> PARAM_CONVERTER_MAP = new LinkedHashMap<>();
    
    static {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        PARAM_CONVERTER_MAP.put(Byte.class,       new AbstractParamConverter<Byte>(Byte.class) {
            @Override
            public Byte _getParam(String strVal) throws BadRequestException {
                return Byte.parseByte(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Short.class,      new AbstractParamConverter<Short>(Short.class) {
            @Override
            protected Short _getParam(String strVal) throws BadRequestException {
                return Short.parseShort(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Integer.class,    new AbstractParamConverter<Integer>(Integer.class) {
            @Override
            protected Integer _getParam(String strVal) throws BadRequestException {
                return Integer.parseInt(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Long.class,       new AbstractParamConverter<Long>(Long.class) {
            @Override
            protected Long _getParam(String strVal) throws BadRequestException {
                return Long.parseLong(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Float.class,      new AbstractParamConverter<Float>(Float.class) {
            @Override
            protected Float _getParam(String strVal) throws BadRequestException {
                return Float.parseFloat(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Double.class,     new AbstractParamConverter<Double>(Double.class) {
            @Override
            protected Double _getParam(String strVal) throws BadRequestException {
                return Double.parseDouble(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(BigDecimal.class, new AbstractParamConverter<BigDecimal>(BigDecimal.class) {
            @Override
            protected BigDecimal _getParam(String strVal) throws BadRequestException {
                return new BigDecimal(strVal);
            }
        });
        PARAM_CONVERTER_MAP.put(Date.class,       new AbstractParamConverter<Date>(Date.class) {
            
            
            @Override
            protected Date _getParam(String strVal) throws BadRequestException {
                try {
                    return sdf.parse(strVal);
                } catch (ParseException ex) {
                    throw new BadRequestException(ex.getMessage());
                }
            }
        });
        PARAM_CONVERTER_MAP.put(String.class,     new AbstractParamConverter<String>(String.class, false) {
            @Override
            protected String _getParam(String strVal) throws BadRequestException {
                return strVal;
            }
        });
        
    }
    //</editor-fold>
    
    private final ActionRequest       wrappedRequest;
    private final HttpMethod          method;
    private final String              requestUri;
    private final Map<String, String> paramMap;
    
    private String         pathInfo;
    private ActionResponse response;

    // Generic constructor
    private ActionRequest(ActionRequest wrappedRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws MethodNotAllowedException {
        super(servletRequest, servletResponse);
        this.wrappedRequest = wrappedRequest;

        try {
            this.method = HttpMethod.valueOf(servletRequest.getMethod());
        } catch (IllegalArgumentException ex) {
            throw new MethodNotAllowedException();
        }

        requestUri = servletRequest.getRequestURI();
        
        if (wrappedRequest != null) {
            
            //<editor-fold defaultstate="collapsed" desc="Wrapper">
            pathInfo             = wrappedRequest.pathInfo;
            paramMap             = wrappedRequest.paramMap;
            response             = wrappedRequest.response;
            //</editor-fold>
            
        } else { 
            
            //<editor-fold defaultstate="collapsed" desc="First level constructor">
            String pathInfo = servletRequest.getPathInfo();
            this.pathInfo = pathInfo == null ? "/" : pathInfo;
            Map<String, String> tmpParameters = new LinkedHashMap<>();
            for (Map.Entry<String, String[]> entry : servletRequest.getParameterMap().entrySet()) {
                String[] values = entry.getValue();
                tmpParameters.put(entry.getKey(), values[values.length - 1]);
            }
            paramMap = Collections.unmodifiableMap(tmpParameters);
            //</editor-fold>
            
        }
        
    }

    // First level constructor...
    public ActionRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this(null, servletRequest, servletResponse);
    }

    // Simple wrapper constructor...
    public ActionRequest(ActionRequest wrappedRequest) {
        this(wrappedRequest, wrappedRequest.getServletRequest(), wrappedRequest.getServletResponse());
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
        
        T t;
        
        if (Enum.class.isAssignableFrom(targetClass)) {
            t = __getEnum(targetClass, strVal);
        } else {
            ParamConverter<T> converter = PARAM_CONVERTER_MAP.get(targetClass);
            
            if (converter == null)
                throw new UnsupportedOperationException("There is no converter for " + targetClass.getName());

            t = converter.getParam(strVal);
        }
        
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
        
        T t;
        
        if (Enum.class.isAssignableFrom(targetClass)) {
            t = __getEnum(targetClass, strVal);
        } else {
            ParamConverter<T> converter = PARAM_CONVERTER_MAP.get(targetClass);
        
            if (converter == null)
                throw new UnsupportedOperationException("There is no converter for " + targetClass.getName());
            
            t = converter.getParam(strVal);
        }
        
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
    
    public final ActionResponse getResponse() {
        return response;
    }

    
    final void _setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }
    
    final void _setResponse(ActionResponse response) {
        this.response = response;
    }
    
    
    @Override
    public String toString() {
        return getRequestLine();
    }

}
