package com.agapsys.rcf;

import com.agapsys.rcf.exceptions.ClientException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionServlet<HE extends HttpExchange> extends HttpServlet {

	private final ActionDispatcher actionDispatcher = new ActionDispatcher();
	private final LazyInitializer lazyInitializer = new LazyInitializer() {
		@Override
		protected void onInitialize() {
			synchronized (ActionServlet.class) {
				onInit();
			}
		}
	};

	/**
	 * Called during servlet initialization.
	 *
	 * This is the place to register actions. This method will be called only once during servlet instance life-cycle.
	 */
	protected void onInit() {}

	/**
	 * Called upon endpoint not found.
	 *
	 * @param exchange HTTP exchange. Default implementation sends a {@linkplain HttpServletResponse#SC_NOT_FOUND} status.
	 * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
	 * @throws ServletException if the HTTP request cannot be handled.
	 */
	protected void onNotFound(HE exchange) throws ServletException, IOException {
		exchange.getCoreResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * Called when an uncaught error happens while processing an action. Default implementation does nothing.
	 *
	 * @param exchange HTTP exchange.
	 * @param throwable error.
	 * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
	 * @throws ServletException if the HTTP request cannot be handled.
	 * @return a boolean indicating if given error was handled. Default implementation returns false.
	 */
	protected boolean onUncaughtError(HE exchange, Throwable throwable) throws ServletException, IOException {
		return false;
	}

	/**
	 * Called upon a error thrown due to client request.
	 *
	 * @param exchange HTTP exchange. Default implementation sends a 4XX status according to given exception.
	 * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
	 * @throws ServletException if the HTTP request cannot be handled
	 */
	protected void onClientError(HE exchange, ClientException error) throws ServletException, IOException {
		HttpServletResponse coreResp = exchange.getCoreResponse();

		coreResp.setStatus(error.getHttpStatus());
		Integer appStatus = error.getAppStatus();
		coreResp.getWriter().printf(
			"%s%s",
			appStatus == null ? "" : String.format("%d:", appStatus),
			error.getMessage()
		);
	}

	/**
	 * Register an action
	 *
	 * @param method HTTP method
	 * @param path path relative to this provider Servlet mapping
	 * @param action action associated with given parameters.
	 */
	protected void registerAction(HttpMethod method, String path, Action action) {

		if (lazyInitializer.isInitialized())
			throw new IllegalStateException("Servlet is already initialized");

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		actionDispatcher.registerAction(action, method, path);
	}

	/**
	 * Called before an action. This method will be called only if an action associated to given request is found and it it allowed to be processed (see {@link SecurityManager}). Default implementation does nothing.
	 *
	 * @param exchange HTTP exchange.
	 * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
	 * @throws ServletException if the HTTP request cannot be handled.
	 */
	protected void beforeAction(HE exchange) throws ServletException, IOException {}

	/**
	 * Called after an action. This method will be called only if an action associated to given request is found, the action is allowed to be processed (see {@link SecurityManager}), and the action was successfully processed. Default implementation does nothing.
	 *
	 * @param exchange HTTP exchange.
	 * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
	 * @throws ServletException if the HTTP request cannot be handled.
	 */
	protected void afterAction(HE exchange) throws ServletException, IOException {}

	/**
	 * Returns the HTTP exchange used by this servlet.
	 *
	 * @param req HTTP request.
	 * @param resp HTTP response.
	 * @return HTTP exchange.
	 */
	protected HE getHttpExchange(HttpServletRequest req, HttpServletResponse resp) {
		return (HE) new HttpExchange(req, resp);
	}

	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!lazyInitializer.isInitialized()) {
			lazyInitializer.initialize();
		}

		Action action = actionDispatcher.getAction(req);

		HE exchange = getHttpExchange(req, resp);

		if (action == null) {
			onNotFound(exchange);
		} else {
			try {
				beforeAction(exchange);
				action.processRequest(exchange);
				afterAction(exchange);
			} catch (ClientException ex) {
				onClientError(exchange, ex);
			} catch (Throwable ex) {
				if (!onUncaughtError(exchange, ex)) {
					if (ex instanceof RuntimeException) {
						throw (RuntimeException) ex;
					}

					if (ex instanceof ServletException) {
						throw (ServletException) ex;
					}

					if (ex instanceof IOException) {
						throw (IOException) ex;
					}

					throw new ServletException(ex);
				}
			}
		}
	}
}
