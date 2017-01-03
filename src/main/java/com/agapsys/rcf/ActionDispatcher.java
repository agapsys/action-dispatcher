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

import com.agapsys.rcf.exceptions.NotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletException;

/**
 * Action dispatcher.
 *
 * The dispatcher is responsible by mapping request to actions.
 */
public class ActionDispatcher {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    private static final String PATH_PATTERN = "^/\\*?$|^/([a-zA-Z]+[a-zA-Z0-9_]*)+(/\\*)?$";

    private static String __getWildcardPath(String path) {
        if (path.endsWith("/*"))
            return path;

        if (path.endsWith("/"))
            return path + "*";

        return path + "/*";
    }
    // =========================================================================
    // </editor-fold>

    private final Map<HttpMethod, Map<String, Action>> actionMap = new LinkedHashMap<>();

    /**
     * Registers an action with given URL.
     * @param method associated HTTP method.
     * @param path relative path associated with given action.
     * @param action action to be associated with given path and HTTP method.
     * @return this.
     */
    public synchronized ActionDispatcher registerAction(HttpMethod method, String path, Action action) {
        if (method == null)
            throw new IllegalArgumentException("Null method");

        if (!Pattern.matches(PATH_PATTERN, path))
            throw new IllegalArgumentException(String.format("Invalid path: '%s'", path));

        if (action == null)
            throw new IllegalArgumentException("Null action");

        Map<String, Action> methodMap = actionMap.get(method);

        if (methodMap == null) {
            methodMap = new LinkedHashMap<>();
            actionMap.put(method, methodMap);
        }

        if (methodMap.containsKey(path))
            throw new IllegalArgumentException(String.format("Mapping already exists: %s %s", method, path));

        methodMap.put(path, action);
        return this;
    }

    /** Removes all registered actions. */
    public synchronized void clearActions() {
        actionMap.clear();
    }

    /**
     * Dispatches a request to an action.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     * @throws NotFoundException if there is not action to process given request.
     */
    public synchronized void dispatch(HttpRequest request, HttpResponse response) throws ServletException, IOException, NotFoundException {
        String uri = request.getRequestUri();
        int secondSlashIndex = uri.indexOf("/", 1);

        String actionPath = secondSlashIndex == -1 ? uri : uri.substring(0, secondSlashIndex);

        Map<String, Action> methodMap = actionMap.get(request.getMethod());

        Action action;
        boolean usingWildcard = false;

        action = methodMap.get(actionPath);

        if (action == null) { // <-- Test for wildcard action
            action = methodMap.get(__getWildcardPath(actionPath));

            if (action != null) {
                usingWildcard = true;
            } else { // <-- Test for root-wildcard action
                action = methodMap.get("/*");

                if (action != null) {
                    actionPath = "/";
                    usingWildcard = true;
                }
            }
        }

        if (action == null)
            throw new NotFoundException();

        if (actionPath.equals(uri) && !uri.endsWith("/") && usingWildcard) { // <-- mapping: '/foo/*', uri: '/foo[?query=string]'. => redirects to '/foo/[?query=string]'
            String queryString = request.getQueryString();
            String redirectPath = request.getRequestUri() + "/";
            if (queryString != null)
                redirectPath = redirectPath + "?" + queryString;

            response.sendRedirect(redirectPath);
        } else {
            if (!usingWildcard && !uri.equals(actionPath)) { // <-- mapping: '/foo', uri: '/foo/[?query=string]'. => redirects to '/foo[?query=string]'
                if (HttpRequest._getRelativePath(actionPath, uri).equals("/")) {
                    String redirectPath = uri.substring(0, uri.length() - 1);
                    String queryString = request.getQueryString();
                    if (queryString != null)
                        redirectPath = redirectPath + "?" + queryString;

                    response.sendRedirect(redirectPath);
                } else {
                    throw new NotFoundException();
                }
            } else {
                beforeAction(request, response);
                action.processRequest(new HttpRequest(actionPath, request), response);
                afterAction(request, response);
            }
        }
    }

    /**
     * Called before an action. Default implementation does nothing.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    protected void beforeAction(HttpRequest request, HttpResponse response) throws ServletException, IOException {}

    /**
     * Called after an action processing. Default implementation does nothing.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    protected void afterAction(HttpRequest request, HttpResponse response) throws ServletException, IOException {}

}
