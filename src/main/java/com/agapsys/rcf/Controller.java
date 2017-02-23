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

import com.agapsys.rcf.exceptions.ClientException;
import com.agapsys.rcf.exceptions.ForbiddenException;
import com.agapsys.rcf.exceptions.UnauthorizedException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet responsible by mapping methods to actions
 */
public class Controller extends ActionServlet {

    // <editor-fold desc="STATIC SCOPE">
    // ========================================================================

    // <editor-fold desc="Private static members" defaultstate="collapsed">
    // -------------------------------------------------------------------------
    private static final Set<String> EMPTY_ROLE_SET = Collections.unmodifiableSet(new LinkedHashSet<String>());
    private static final Object[]    EMPTY_OBJ_ARRAY = new Object[] {};

    // Validates a candidate method to be interpreted as an action
    private static class MethodActionValidator {

        private static enum ArgGoal {
            INVALID (new Class[] {} ),
            REQUEST (new Class[] { ActionRequest.class, HttpServletRequest.class } ),
            RESPONSE(new Class[] { ActionResponse.class, HttpServletResponse.class} );

            private static ArgGoal __lookup(Class tested) {
                for (ArgGoal goal : ArgGoal.values()) {

                    for (Class c : goal.supportedClasses) {
                        if (c.isAssignableFrom(tested))
                            return goal;
                    }
                }

                return INVALID;
            }

            private static ArgGoal __lookup(String testedClassName) {
                try {
                    return __lookup(Class.forName(testedClassName));
                } catch (ClassNotFoundException ex) {
                    return INVALID;
                }
            }

            private final Class[] supportedClasses;

            private ArgGoal(Class[] supportedClasses) {
                this.supportedClasses = supportedClasses;
            }

        }

        /**
         * Checks if an annotated method signature matches with required one.
         *
         * @param method annotated method.
         * @return boolean indicating if method signature is valid.
         */
        private static boolean __matchSignature(Method method) {
            String signature = method.toGenericString();
            String[] tokens = signature.split(Pattern.quote(" "));

            if (!tokens[0].equals("public")) {
                return false;
            }

            int indexOfOpenParenthesis = signature.indexOf("(");
            int indexOfCloseParenthesis = signature.indexOf(")");

            String argString = signature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis).trim();
            String[] args = argString.isEmpty() ? new String[0] : argString.split(Pattern.quote(","));

            switch (args.length) {
                case 0:
                    return true;

                case 1:
                    return ArgGoal.__lookup(args[0]) != ArgGoal.INVALID;

                case 2: {
                    ArgGoal arg0Goal = ArgGoal.__lookup(args[0]);
                    ArgGoal arg1Goal = ArgGoal.__lookup(args[1]);

                    return !(arg0Goal == ArgGoal.INVALID || arg1Goal == ArgGoal.INVALID || (arg0Goal == arg1Goal));
                }

                default:
                    return false;
            }
        }

        private static Object[] __getCallParams(Method method, ActionRequest request, ActionResponse response) {
            if (method.getParameterCount() == 0) return EMPTY_OBJ_ARRAY;

            List argList = new LinkedList();

            for (Class<?> type : method.getParameterTypes()) {
                
                if (JsonRequest.class.isAssignableFrom(type)) {
                    argList.add(new JsonRequest(request));
                    continue;
                }
                
                if (JsonResponse.class.isAssignableFrom(type)) {
                    argList.add(new JsonResponse(response));
                    continue;
                }
                

                if (ActionRequest.class.isAssignableFrom(type)) {
                    argList.add(request);
                    continue;
                }

                if (ActionResponse.class.isAssignableFrom(type)) {
                    argList.add(response);
                    continue;
                }

                if (HttpServletRequest.class.isAssignableFrom(type)) {
                    argList.add(request.getServletRequest());
                    continue;
                }

                if (HttpServletResponse.class.isAssignableFrom(type)) {
                    argList.add(response.getServletResponse());
                    continue;
                }

                throw new UnsupportedOperationException(String.format("Unsupported param type: %s", type.getName()));
            }

            return argList.toArray();
        }
    }
    // -------------------------------------------------------------------------
    // </editor-fold>

    /** Defines a Data Transfer Object */
    public static interface Dto<T> {

        /**
         * Returns a transfer object associated with this instance.
         *
         * @return a transfer object associated with this instance.
         */
        public T getDto();
    }

    /** Name of the session attribute used to store current user. */
    public static final String SESSION_ATTR_USER = Controller.class.getName() + ".SESSION_ATTR_USER";

    /** Name of the default session attribute used to store CSRF token. */
    public static final String SESSION_ATTR_CSRF_TOKEN = Controller.class.getName() + ".SESSION_ATTR_CSRF_TOKEN";

    /** Name of the header used to send/retrieve a CSRF token. */
    public static final String CSRF_HEADER  = "X-Csrf-Token";

    // Default size of CSRF token
    private static final int CSRF_TOKEN_LENGTH = 128;

    private static String __getRandom(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        return __getRandom(length, chars);
    }

    private static String __getRandom(int length, char[] chars)  {
        if (length < 1) throw new IllegalArgumentException("Invalid length: " + length);

        if (chars == null || chars.length == 0) throw new IllegalArgumentException("Null/Empty chars");

        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        return sb.toString();
    }
    // =========================================================================
    // </editor-fold>

    private class MethodCallerAction implements Action {

        private final String[] requiredRoles;
        private final Method method;
        private final boolean secured;

        private MethodCallerAction(Method method, boolean secured, String[] requiredRoles) {
            this.method = method;
            this.requiredRoles = requiredRoles;
            this.secured = secured || requiredRoles.length > 0;
        }

        private void __checkSecurity(ActionRequest request, ActionResponse response) throws ServletException, IOException, UnauthorizedException, ForbiddenException {
            if (secured) {
                User user = getUser(request);

                if (user == null)
                    throw new UnauthorizedException("Unauthorized");

                Set<String> userRoles = user.getRoles();

                if (userRoles == null)
                    userRoles = EMPTY_ROLE_SET;

                for (String requiredRole : requiredRoles) {
                    if (!userRoles.contains(requiredRole))
                        throw new ForbiddenException();
                }
            }
        }

        private Object __getSingleDto(Object obj) {
            if (obj == null)
                return null;

            if (obj instanceof Dto)
                return ((Dto) obj).getDto();

            return obj;
        }

        private List __getDtoList(List objList) {
            List dto = new LinkedList();

            for (Object obj : objList) {
                dto.add(__getSingleDto(obj));
            }

            return dto;
        }

        private Map __getDtoMap(Map<Object, Object> objMap) {
            Map dto = new LinkedHashMap();

            for (Map.Entry entry : objMap.entrySet()) {
                dto.put(__getSingleDto(entry.getKey()), __getSingleDto(entry.getValue()));
            }

            return dto;
        }

        private Set __getDtoSet(Set objSet) {
            Set dto = new LinkedHashSet();

            for (Object obj : objSet) {
                dto.add(__getSingleDto(obj));
            }

            return dto;
        }

        private Object __getDtoObject(Object src) {

            Object dto;

            if (src instanceof List) {
                dto = __getDtoList((List) src);
            } else if (src instanceof Set) {
                dto = __getDtoSet((Set) src);
            } else if (src instanceof Map) {
                dto = __getDtoMap((Map<Object, Object>) src);
            } else {
                dto = __getSingleDto(src);
            }

            return dto;
        }

        @Override
        public void processRequest(ActionRequest request, ActionResponse response) throws ServletException, IOException {
            try {
                __checkSecurity(request, response);

                Object[] callParams = MethodActionValidator.__getCallParams(method, request, response);

                Object returnedObj = method.invoke(Controller.this, callParams);

                if (returnedObj == null && method.getReturnType().equals(Void.TYPE))
                    return;

                sendObject(request, response, __getDtoObject(returnedObj));

            } catch (InvocationTargetException | IllegalAccessException ex) {
                if (ex instanceof InvocationTargetException) {
                    Throwable targetException = ((InvocationTargetException) ex).getTargetException();

                    if (targetException instanceof ClientException) {
                        throw (ClientException) targetException;
                    } else {
                        throw new RuntimeException(targetException);
                    }
                }

                throw new RuntimeException(ex);
            }
        }

    }

    @Override
    protected final void onInit() {
        super.onInit();

        Class<? extends Controller> actionServletClass = Controller.this.getClass();

        // Check for WebAction annotations...
        Method[] methods = actionServletClass.getDeclaredMethods();

        for (Method method : methods) {
            WebActions webActionsAnnotation = method.getAnnotation(WebActions.class);
            WebAction[] webActions;

            if (webActionsAnnotation == null) {
                WebAction webAction = method.getAnnotation(WebAction.class);
                if (webAction == null) {
                    webActions = new WebAction[]{};
                } else {
                    webActions = new WebAction[]{webAction};
                }
            } else {
                webActions = webActionsAnnotation.value();
            }

            for (WebAction webAction : webActions) {
                if (!MethodActionValidator.__matchSignature(method)) {
                    throw new RuntimeException(String.format("Invalid action signature (%s).", method.toGenericString()));
                }

                HttpMethod[] httpMethods = webAction.httpMethods();
                String path = webAction.mapping().trim();

                if (path.isEmpty()) {
                    path = "/" + method.getName();
                }

                MethodCallerAction action = new MethodCallerAction(method, webAction.secured(), webAction.requiredRoles());

                for (HttpMethod httpMethod : httpMethods) {
                    registerAction(httpMethod, path, action);
                }
            }
        }

        onControllerInit();
    }

    /**
     * Called during controller initialization. Default implementation does nothing.
     */
    protected void onControllerInit() {}

    /**
     * Called upon controller uncaught error.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param uncaughtError uncaught error.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     * @return a boolean indicating if given error shall be propagated. Default implementation just returns true.
     */
    protected boolean onControllerError(ActionRequest request, ActionResponse response, Throwable uncaughtError) throws ServletException, IOException {
        return true;
    }

    /**
     * This method instructs the controller how to retrieve the user associated with given HTTP exchange.
     *
     * @param request HTTP request.
     * @return an user associated with given request. Default uses servlet request session to retrive the user. If a user cannot be retrieved from given request, returns null. Default implementation also verify CSRF attacks.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    protected User getUser(ActionRequest request) throws ServletException, IOException {
        HttpSession session = request.getServletRequest().getSession(false);

        if (session == null)
            return null;

        User user = (User) session.getAttribute(SESSION_ATTR_USER);

        if (user == null)
            return null;

        String sessionToken = (String) session.getAttribute(SESSION_ATTR_CSRF_TOKEN);
        String requestToken = request.getHeader(CSRF_HEADER);

        if (!Objects.equals(sessionToken, requestToken))
            return null;

        return user;
    }

    /**
     * This method instructs the controller how to associate an user with a HTTP exchange.
     *
     * Default implementation uses servlet request session associated with given request.
     * @param request HTTP request.
     * @param response HTTP response.
     * @param user user to be registered with given HTTP exchange. Passing null unregisters the user. Passing non-null user instance will generate a CSRF token header which will be sent in associated response.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    protected void setUser(ActionRequest request, ActionResponse response, User user) throws ServletException, IOException {

        if (user == null) {
            HttpSession session = request.getServletRequest().getSession(false);

            if (session != null) {
                session.removeAttribute(SESSION_ATTR_USER);
                session.removeAttribute(SESSION_ATTR_CSRF_TOKEN);
            }

        } else {
            HttpSession session = request.getServletRequest().getSession();
            session.setAttribute(SESSION_ATTR_USER, user);

            String csrfToken = __getRandom(CSRF_TOKEN_LENGTH);
            session.setAttribute(SESSION_ATTR_CSRF_TOKEN, csrfToken);
            response.addHeader(CSRF_HEADER, csrfToken);
        }

    }

    /**
     * This method instructs the controller how to send an object to the client.
     *
     * Default implementation serializes the DTO into a JSON response.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param obj object to be sent to the client.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    protected void sendObject(ActionRequest request, ActionResponse response, Object obj) throws ServletException, IOException {
        new JsonResponse(response).sendObject(obj);
    }

    @Override
    protected final boolean onUncaughtError(ActionRequest request, ActionResponse response, RuntimeException uncaughtError) throws ServletException, IOException {
        super.onUncaughtError(request, response, uncaughtError);

        Throwable cause = uncaughtError.getCause(); // <-- MethodCallerAction throws the target exception wrapped in a RuntimeException

        if (cause == null)
            cause = uncaughtError;

        if (cause instanceof ServletException)
            throw (ServletException) cause;

        if (cause instanceof IOException)
            throw (IOException) cause;

        return onControllerError(request, response, cause);
    }

}
