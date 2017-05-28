package com.agapsys.rcf;

import com.agapsys.rcf.exceptions.ClientException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionServlet extends HttpServlet {

    private final ActionDispatcher actionDispatcher = new ActionDispatcher() {

        @Override
        protected void beforeAction(ActionRequest request, ActionResponse response) throws ServletException, IOException {
            super.beforeAction(request, response);
            ActionServlet.this.beforeAction(request, response);
        }

        @Override
        protected void afterAction(ActionRequest request, ActionResponse response) throws ServletException, IOException {
            super.afterAction(request, response);
            ActionServlet.this.afterAction(request, response);
        }

    };
    private boolean initialized = false;

    /**
     * Returns a boolean indicating this servlet initialization status.
     *
     * @return a boolean indicating this servlet initialization status.
     */
    protected synchronized boolean isInitialized() {
        return initialized;
    }

    private synchronized void __init() {
        if (!isInitialized()) {
            onInit();
            initialized = true;
        }
    }

    /**
     * Called during servlet initialization.
     *
     * This is the ideal place to register actions. This method will be called only once during servlet instance life-cycle.
     */
    protected void onInit() {}

    /**
     * Register an action.
     *
     * @param method HTTP method.
     * @param path path relative to this provider Servlet mapping.
     * @param action action associated with given parameters.
     */
    protected synchronized void registerAction(HttpMethod method, String path, Action action) {
        actionDispatcher.registerAction(method, path, action);
    }

    /**
     * Called when an uncaught error happened while processing the request.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param uncaughtError uncaught error.
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     * @return a boolean indicating if given error shall be propagated. Default implementation just returns true. In order to suppress the error, implementation should return false.
     */
    protected boolean onUncaughtError(ActionRequest request, ActionResponse response, RuntimeException uncaughtError) throws ServletException, IOException {
        return true;
    }

    /**
     * Called upon a client error.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param error client error.
     * @throws ServletException if the HTTP request cannot be handled
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    protected void onClientError(ActionRequest request, ActionResponse response, ClientException error) throws ServletException, IOException {
        response.setStatus(error.getHttpStatus());
        
        HttpServletResponse servletResponse = response.getServletResponse();
        
        servletResponse.setContentType("text/plain");
        servletResponse.setCharacterEncoding("UTF-8");
        
        Integer appStatus = error.getAppStatus();
        servletResponse.getWriter().printf(
            "%s%s",
            appStatus == null ? "" : String.format("%d:", appStatus),
            error.getMessage()
        );
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
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     * @throws ServletException if the HTTP request cannot be handled.
     */
    protected void afterAction(ActionRequest request, ActionResponse response) throws ServletException, IOException {}

    
    /**
     * Returns the action request instance used by this servlet.
     * 
     * @param request raw HTTP request.
     * @param response raw HTTP response.
     * @return the action request instance used by this servlet.
     */
    protected ActionRequest getActionRequest(HttpServletRequest request, HttpServletResponse response) {
        return new ActionRequest(request, response);
    }
    
    /**
     * Returns the action response instance used by this servlet.
     * 
     * @param request raw HTTP request.
     * @param response raw HTTP response.
     * @return the action response instance used by this servlet.
     */
    protected ActionResponse getActionResponse(HttpServletRequest request, HttpServletResponse response) {
        return new ActionResponse(request, response);
    }
    
    @Override
    protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        __init();

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains("//")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (pathInfo == null && !req.getRequestURI().endsWith("/")) {
            resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            String queryString = req.getQueryString();
            if (queryString == null)
                queryString = "";

            String location = req.getRequestURI() + "/";
            if (!queryString.isEmpty())
                location = String.format("%s?%s", location, queryString);

            resp.addHeader("Location", location);
            return;
        }

        ActionRequest request;
        ActionResponse response;

       try {
           request = getActionRequest(req, resp);
           response = getActionResponse(req, resp);
           
           request._setResponse(response);
           response._setRequest(request);
           
       } catch (ClientException ex) {
           resp.setStatus(ex.getHttpStatus());
           return;
       }

        try {
            actionDispatcher.dispatch(request, response);
        } catch (ClientException ex) {
            onClientError(request, response, ex);
        } catch (RuntimeException ex) {
            if (onUncaughtError(request, response, ex))
                throw ex;
        }
    }
}
