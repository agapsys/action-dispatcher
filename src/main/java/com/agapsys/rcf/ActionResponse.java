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

import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionResponse extends ServletExchange {

    private final ActionResponse wrappedResponse;

    // Generic constructor
    ActionResponse(ActionResponse wrappedResponse, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        super(servletRequest, servletResponse);
        this.wrappedResponse = wrappedResponse;
    }

    ActionResponse(HttpServletRequest serlvetRequest, HttpServletResponse servletResponse) {
        this(null, serlvetRequest, servletResponse);
    }

    protected ActionResponse(ActionResponse wrappedResponse) {
        this(wrappedResponse, wrappedResponse._getServletRequest(), wrappedResponse.getServletResponse());
    }

    protected final ActionResponse getWrappedResponse() {
        return wrappedResponse;
    }

    /**
     * Adds a cookie.
     *
     * @param name cookie name
     * @param value cookie value
     * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
     * @param path cookie path (usually {@linkplain HttpServletRequest#getContextPath()})
     * @return this
     */
    public final ActionResponse addCookie(String name, String value, int maxAge, String path) {
        if (path == null || !path.startsWith("/"))
            throw new IllegalArgumentException("Invalid path: " + path);

        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        getServletResponse().addCookie(cookie);
        return this;
    }

    /**
     * Adds a cookie for request context path.
     *
     * @param name cookie name
     * @param value cookie value
     * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
     * @return this
     */
    public final ActionResponse addCookie(String name, String value, int maxAge) {
        addCookie(name, value, maxAge, _getServletRequest().getContextPath());
        return this;
    }

    /**
     * Removes a cookie.
     *
     * @param name name of the cookie to be removed
     * @param path cookie path ((usually {@linkplain HttpServletRequest#getContextPath()})
     * @return this
     */
    public final ActionResponse removeCookie(String name, String path) {
        addCookie(name, null, 0, path);
        return this;
    }

    public final ActionResponse setHeader(String name, String value) {
        _getServletResponse().setHeader(name, value);
        return this;
    }

    public final ActionResponse addHeader(String name, String value) {
        _getServletResponse().addHeader(name, value);
        return this;
    }

    /**
     * Removes a cookie for request context path.
     *
     * @param name name of the cookie to be removed
     * @return this
     */
    public final ActionResponse removeCookie(String name) {
        removeCookie(name, _getServletRequest().getContextPath());
        return this;
    }

    public final ActionResponse sendTemporaryRedirect(String location) throws IOException {
        getServletResponse().sendRedirect(location);
        return this;
    }

    public final ActionResponse sendPermanentRedirect(String location) throws IOException {
        setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        addHeader("Location", location);
        return this;
    }

    public final ActionResponse setStatus(int status) throws IOException {
        getServletResponse().setStatus(status);
        return this;
    }

    public final HttpServletResponse getServletResponse() {
        return _getServletResponse();
    }

}
