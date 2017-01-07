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
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ActionRequest extends ServletExchange {

    static String _getRelativePath(String parent, String child) {
        if (parent.endsWith("/"))
            parent = parent.substring(0, parent.length() - 1);

        if (child.endsWith("/"))
            child = child.substring(0, child.length() - 1);

        String tmpPath = child.replaceFirst(Pattern.quote(parent), "");
        return tmpPath.startsWith("/") ? tmpPath : "/" + tmpPath;
    }

    private final ActionRequest         wrappedRequest;
    private final HttpMethod          method;
    private final String              requestUri;
    private final String              pathInfo;
    private final Map<String, String> paramMap;
    private final Map<String, Object> metadata;

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
            metadata = new LinkedHashMap<>();
            Map<String, String> tmpParameters = new LinkedHashMap<>();
            for (Map.Entry<String, String[]> entry : servletRequest.getParameterMap().entrySet()) {
                String[] values = entry.getValue();
                tmpParameters.put(entry.getKey(), values[values.length - 1]);
            }
            paramMap = Collections.unmodifiableMap(tmpParameters);
        } else { // <-- wrappedRequest != null && parentPath != null
            pathInfo = _getRelativePath(parentPath, wrappedRequest.pathInfo);
            metadata = wrappedRequest.metadata;
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

        String val = getServletRequest().getParameter(paramName);
        if (val == null || val.trim().isEmpty())
            val = defaultValue;

        if (val != null)
            val = val.trim();

        return val;
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
        String val = getServletRequest().getParameter(paramName);

        if (val == null || val.trim().isEmpty()) {
            if (errMsgArgs.length > 0)
                errorMessage = String.format(errorMessage, errMsgArgs);

            throw new BadRequestException(errorMessage);
        }

        return val;
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

    public final Object putMetadata(String key, Object value) {
        return metadata.put(key, value);
    }

    public final Object getMetadata(String key) {
        return metadata.get(key);
    }

    public final Object removeMetadata(String key) {
        return metadata.remove(key);
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
