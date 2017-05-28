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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    
    private static String __getRelativePath(String parent, String child) {
        if (parent.endsWith("/"))
            parent = parent.substring(0, parent.length() - 1);

        if (child.endsWith("/"))
            child = child.substring(0, child.length() - 1);

        String tmpPath = child.replaceFirst(Pattern.quote(parent), "");
        return tmpPath.startsWith("/") ? tmpPath : "/" + tmpPath;
    }
    
    private ActionRequest __getWrapped(String parentPath, ActionRequest wrappedRequest) {
        try {
            Constructor constructor = wrappedRequest.getClass().getConstructor(ActionRequest.class);
            ActionRequest customRequest = (ActionRequest) constructor.newInstance(wrappedRequest);
            String pathInfo = __getRelativePath(parentPath, wrappedRequest.getPathInfo());
            customRequest._setPathInfo(pathInfo);
            
            return customRequest;
            
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(String.format("Cannot create request instance for %s", wrappedRequest.getClass().getName()));
        }

    }
    
    // TODO make test
    public static void main(String[] args) {
        String child = "/foo/path/to/resource";
        String parent = "/foo/path";
        String relativePath = __getRelativePath(parent, child);

        child = "/foo/path";
        parent = "/bar/path";
        relativePath = __getRelativePath(parent, child);

        child = "/abc";
        parent = "/";
        relativePath = __getRelativePath(parent, child);

        child = "/";
        parent = "/";
        relativePath = __getRelativePath(parent, child);

        child = "/abc/";
        parent = "/abc";
        relativePath = __getRelativePath(parent, child);

        child = "/abc";
        parent = "/abc/";
        relativePath = __getRelativePath(parent, child);

        return;
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
    public synchronized void dispatch(ActionRequest request, ActionResponse response) throws ServletException, IOException, NotFoundException {
        String pathInfo = request.getPathInfo();
        int secondSlashIndex = pathInfo.indexOf("/", 1);

        String actionPath = secondSlashIndex == -1 ? pathInfo : pathInfo.substring(0, secondSlashIndex);

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

        if (actionPath.equals(pathInfo) && !pathInfo.endsWith("/") && usingWildcard) { // <-- mapping: '/foo/*', uri: '/foo[?query=string]'. => redirects to '/foo/[?query=string]'
            String queryString = request.getQueryString();
            String redirectPath = request.getRequestUri() + "/";
            if (queryString != null)
                redirectPath = redirectPath + "?" + queryString;

            response.sendPermanentRedirect(redirectPath);
        } else {
            if (!usingWildcard && !pathInfo.equals(actionPath)) { // <-- mapping: '/foo', uri: '/foo/[?query=string]'. => redirects to '/foo[?query=string]'
                if (__getRelativePath(actionPath, pathInfo).equals("/")) {

                    String requestUri = request.getRequestUri();
                    String redirectPath = requestUri.substring(0, requestUri.length() - 1);
                    String queryString = request.getQueryString();
                    if (queryString != null)
                        redirectPath = redirectPath + "?" + queryString;

                    response.sendPermanentRedirect(redirectPath);
                } else {
                    throw new NotFoundException();
                }
            } else {
                if (!actionPath.equals("/")) {
                    request = __getWrapped(actionPath, request);
                }

                beforeAction(request, response);
                action.processRequest(request, response);
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
    protected void beforeAction(ActionRequest request, ActionResponse response) throws ServletException, IOException {}

    /**
     * Called after an action processing. Default implementation does nothing.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    protected void afterAction(ActionRequest request, ActionResponse response) throws ServletException, IOException {}
    
}
