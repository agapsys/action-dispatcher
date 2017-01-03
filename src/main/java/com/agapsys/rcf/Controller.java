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

/**
 * Servlet responsible by mapping methods to actions
 */
public class Controller { //extends ActionServlet {
//
//    // <editor-fold desc="STATIC SCOPE">
//    // ========================================================================
//    private static final Set<String> EMPTY_ROLE_SET = Collections.unmodifiableSet(new LinkedHashSet<String>());
//    private static final Object[] EMPTY_OBJ_ARRAY = new Object[] {};
//
//    private static class MethodActionValidator {
//
//        private static final Class[] SUPPORTED_CLASSES = new Class[] {
//            HttpExchange.class,
//            HttpServletRequest.class,
//            HttpServletResponse.class,
//            HttpRequest.class,
//            HttpResponse.class
//        };
//
//        /**
//         * Returns a boolean indicating if a class is supported as an argument of an action method.
//         *
//         * @param tested tested class
//         * @param supportedClasses supported classes
//         * @return a boolean indicating if a class is supported as an argument of an action method.
//         */
//        public static boolean isSupported(Class tested) {
//            for (Class c : SUPPORTED_CLASSES) {
//                if (c.isAssignableFrom(tested)) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//
//        /**
//         * Checks if an annotated method signature matches with required one.
//         *
//         * @param method annotated method.
//         * @return boolean indicating if method signature is valid.
//         */
//        public static boolean matchSignature(Method method) {
//            String signature = method.toGenericString();
//            String[] tokens = signature.split(Pattern.quote(" "));
//
//            if (!tokens[0].equals("public")) {
//                return false;
//            }
//
//            int indexOfOpenParenthesis = signature.indexOf("(");
//            int indexOfCloseParenthesis = signature.indexOf(")");
//
//            String args = signature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis).trim();
//            if (args.indexOf(",") != -1) {
//                return false; // <-- only one arg method is accepted
//            }
//            if (args.isEmpty()) {
//                return true; // <-- accepts no args
//            }
//
//
//            try {
//                Class<?> clazz = Class.forName(args);
//                return isSupported(clazz);
//            } catch (ClassNotFoundException ex) {
//                return false;
//            }
//        }
//
//        public static Object[] getCallParams(Method method, HttpExchange exchange) {
//            if (method.getParameterCount() == 0) return EMPTY_OBJ_ARRAY;
//
//            Class<?> type = method.getParameterTypes()[0];
//
//            if (HttpExchange.class.isAssignableFrom(type))
//                return new Object[] {exchange};
//
//            if (HttpServletRequest.class.isAssignableFrom(type))
//                return new Object[] {exchange.getCoreRequest()};
//
//            if (HttpServletResponse.class.isAssignableFrom(type))
//                return new Object[] {exchange.getCoreResponse()};
//
//            if (HttpRequest.class.isAssignableFrom(type))
//                return new Object[] {exchange.getRequest()};
//
//            if (HttpResponse.class.isAssignableFrom(type))
//                return new Object[] {exchange.getResponse()};
//
//            throw new UnsupportedOperationException(String.format("Unsupported param type: %s", type.getName()));
//        }
//    }
//
//    /**
//     * Defines a Data Transfer Object
//     */
//    public static interface Dto {
//
//        /**
//         * Returns a transfer object associated with this instance.
//         *
//         * @return a transfer object associated with this instance.
//         */
//        public Object getDto();
//    }
//    // =========================================================================
//    // </editor-fold>
//
//    private class MethodCallerAction implements Action {
//
//        private final String[] requiredRoles;
//        private final Method method;
//
//        private MethodCallerAction(Method method, boolean secured, String[] requiredUserRoles) {
//            if (method == null) {
//                throw new IllegalArgumentException("Method cannot be null");
//            }
//
//            if (secured && requiredUserRoles == null) {
//                throw new IllegalArgumentException("requiredUserRoles cannot be null");
//            }
//
//            this.method = method;
//            this.requiredRoles = requiredUserRoles;
//        }
//
//        /**
//         * Creates an unprotected action.
//         *
//         * @param method method associated with the action.
//         */
//        public MethodCallerAction(Method method) {
//            this(method, false, null);
//        }
//
//        /**
//         * Creates a secured action.
//         *
//         * @param method method associated with the action.
//         * @param requiredUserRoles required user roles in order to process the action.
//         */
//        public MethodCallerAction(Method method, String[] requiredUserRoles) {
//            this(method, true, requiredUserRoles);
//        }
//
//        private void checkSecurity(HttpRequest request) throws UnauthorizedException, ForbiddenException {
//            if (requiredRoles != null) {
//                User user = getUser(request);
//
//                if (user == null)
//                    throw new UnauthorizedException("Unauthorized");
//
//                if (requiredRoles.length > 0) {
//                    Set<String> userRoles = user.getRoles();
//                    if (userRoles == null)
//                        userRoles = EMPTY_ROLE_SET;
//
//                    for (String requiredUserRole : requiredRoles) {
//                        if (!userRoles.contains(requiredUserRole))
//                            throw new ForbiddenException();
//                    }
//                }
//            }
//        }
//
//        private Object getSingleDto(Object obj) {
//            if (obj == null)
//                return null;
//
//            if (obj instanceof Dto)
//                return ((Dto) obj).getDto();
//
//            return obj;
//        }
//
//        private List getDtoList(List objList) {
//            List dto = new LinkedList();
//
//            for (Object obj : objList) {
//                dto.add(getSingleDto(obj));
//            }
//
//            return dto;
//        }
//
//        private Map getDtoMap(Map<Object, Object> objMap) {
//            Map dto = new LinkedHashMap();
//
//            for (Map.Entry entry : objMap.entrySet()) {
//                dto.put(getSingleDto(entry.getKey()), getSingleDto(entry.getValue()));
//            }
//
//            return dto;
//        }
//
//        private Set getDtoSet(Set objSet) {
//            Set dto = new LinkedHashSet();
//
//            for (Object obj : objSet) {
//                dto.add(getSingleDto(obj));
//            }
//
//            return dto;
//        }
//
//        private Object getDtoObject(Object src) {
//
//            Object dto;
//
//            if (src instanceof List) {
//                dto = getDtoList((List) src);
//            } else if (src instanceof Set) {
//                dto = getDtoSet((Set) src);
//            } else if (src instanceof Map) {
//                dto = getDtoMap((Map<Object, Object>) src);
//            } else {
//                dto = getSingleDto(src);
//            }
//
//            return dto;
//        }
//
//        @Override
//        public void processRequest(HttpRequest request, HttpResponse response) throws ServletException, IOException {
//            try {
//                checkSecurity(request);
//
//                Object[] callParams = MethodActionValidator.getCallParams(method, exchange);
//
//                Object returnedObj = method.invoke(Controller.this, callParams);
//
//                if (returnedObj == null && method.getReturnType().equals(Void.TYPE))
//                    return;
//
//                exchange.getResponse().writeObject(getDtoObject(returnedObj));
//
//            } catch (InvocationTargetException | IllegalAccessException ex) {
//                if (ex instanceof InvocationTargetException) {
//                    Throwable targetException = ((InvocationTargetException) ex).getTargetException();
//
//                    if (targetException instanceof ClientException) {
//                        throw (ClientException) targetException;
//                    } else {
//                        throw new RuntimeException(targetException);
//                    }
//                }
//
//                throw new RuntimeException(ex);
//            }
//        }
//
//    }
//
//    /**
//     * Registers action methods.
//     */
//    @Override
//    protected final void onInit() {
//        super.onInit();
//
//        Class<? extends Controller> actionServletClass = Controller.this.getClass();
//
//        // Check for WebAction annotations...
//        Method[] methods = actionServletClass.getDeclaredMethods();
//
//        for (Method method : methods) {
//            WebActions webActionsAnnotation = method.getAnnotation(WebActions.class);
//            WebAction[] webActions;
//
//            if (webActionsAnnotation == null) {
//                WebAction webAction = method.getAnnotation(WebAction.class);
//                if (webAction == null) {
//                    webActions = new WebAction[]{};
//                } else {
//                    webActions = new WebAction[]{webAction};
//                }
//            } else {
//                webActions = webActionsAnnotation.value();
//            }
//
//            for (WebAction webAction : webActions) {
//                if (!MethodActionValidator.matchSignature(method)) {
//                    throw new RuntimeException(String.format("Invalid action signature (%s).", method.toGenericString()));
//                }
//
//                HttpMethod[] httpMethods = webAction.httpMethods();
//                String path = webAction.mapping().trim();
//
//                if (path.isEmpty()) {
//                    path = method.getName();
//                }
//
//                MethodCallerAction action;
//
//                boolean isSecured = webAction.secured() || webAction.requiredRoles().length > 0;
//
//                if (!isSecured) {
//                    action = new MethodCallerAction(method);
//                } else {
//                    action = new MethodCallerAction(method, webAction.requiredRoles());
//                }
//
//                for (HttpMethod httpMethod : httpMethods) {
//                    registerAction(httpMethod, path, action);
//
//                    if (webAction.defaultAction()) {
//                        registerAction(httpMethod, ActionDispatcher.ROOT_PATH, action);
//                    }
//                }
//            }
//        }
//
//        onControllerInit();
//    }
//
//    /**
//     * Called during controller initialization. Default implementation does nothing.
//     */
//    protected void onControllerInit() {}
//
//    /**
//     * Called upon controller uncaught error.
//     *
//     * @param request HTTP request.
//     * @param response HTTP response.
//     * @param uncaughtError uncaught error.
//     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
//     * @throws ServletException if the HTTP request cannot be handled.
//     * @return a boolean indicating if given error shall be propagated. Default implementation just returns true.
//     */
//    protected boolean onControllerError(HttpRequest request, HttpResponse response, RuntimeException uncaughtError) throws ServletException, IOException {
//        return true;
//    }
//
//    /**
//     * Returns an user associated with given request.
//     *
//     * @param request HTTP request.
//     * @return an user associated with given request. Default implementation just returns null.
//     */
//    protected User getUser(HttpRequest request) {
//        return null;
//    }
//
//    protected void sendObject(HttpRequest request, HttpResponse response, Dto dto) throws IOException {
//        new JsonResponse(response).sendObject(dto.getDto());
//    }
//
//    @Override
//    protected final boolean onUncaughtError(HttpRequest request, HttpResponse response, RuntimeException uncaughtError) throws ServletException, IOException {
//        super.onUncaughtError(request, response, uncaughtError);
//
//        Throwable cause = uncaughtError.getCause(); // <-- MethodCallerAction throws the target exception wrapped in a RuntimeException
//
//        if (cause == null)
//            cause = uncaughtError;
//
//        if (cause instanceof ServletException)
//            throw (ServletException) cause;
//
//        if (cause instanceof IOException)
//            throw (IOException) cause;
//
//        return onControllerError(request, response, (RuntimeException)cause);
//    }

}
