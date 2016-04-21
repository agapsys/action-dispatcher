package com.agapsys.rcf;

import com.agapsys.rcf.exceptions.ClientException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionServlet extends HttpServlet {

	private final ActionDispatcher actionDispatcher = new ActionDispatcher();
	private final LazyInitializer lazyInitializer = new LazyInitializer() {
		@Override
		protected void onInitialize() {
			synchronized (ActionServlet.class) {
				onInit();
			}
		}
	};

	protected void onInit() {}

	/**
	 * Called upon endpoint not found
	 *
	 * @param exchange HTTP exchange. Default implementation sends a
	 * {@linkplain HttpServletResponse#SC_NOT_FOUND} status.
	 */
	protected void onNotFound(HttpExchange exchange) {
		exchange.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * Called when an uncaught error happens while processing an action. Default
	 * implementation does nothing.
	 *
	 * @param exchange HTTP exchange
	 * @param throwable error
	 * @return a boolean indicating if given error was handled. Default
	 * implementation returns false
	 */
	protected boolean onUncaughtError(HttpExchange exchange, Throwable throwable) {
		return false;
	}

	/**
	 * Called upon a error thrown due to client request. Default implementation
	 * does nothing.
	 *
	 * @param req HTTP request
	 * @param error client error.
	 */
	protected void onClientError(HttpServletRequest req, ClientException error) {
	}

	/**
	 * Register an action
	 *
	 * @param method HTTP method
	 * @param path path relative to this provider Servlet mapping
	 * @param action action associated with given parameters.
	 */
	protected void registerAction(HttpMethod method, String path, Action action) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		actionDispatcher.registerAction(action, method, path);
	}

	/**
	 * Called before an action. This method will be called only if an action
	 * associated to given request is found and it it allowed to be processed
	 * (see {@link SecurityManager}). Default implementation does nothing.
	 *
	 * @param exchange HTTP exchange
	 */
	protected void beforeAction(HttpExchange exchange) {
	}

	/**
	 * Called after an action. This method will be called only if an action
	 * associated to given request is found, the action is allowed to be
	 * processed (see {@link SecurityManager}), and the action was successfully
	 * processed. Default implementation does nothing.
	 *
	 * @param exchange HTTP exchange
	 */
	protected void afterAction(HttpExchange exchange) {
	}

	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!lazyInitializer.isInitialized())
			lazyInitializer.initialize();

		Action action = actionDispatcher.getAction(req);

		HttpExchange exchange = new HttpExchange(req, resp);

		if (action == null) {
			onNotFound(exchange);
		} else {
			try {
				beforeAction(exchange);
				action.processRequest(new HttpExchange(req, resp));
				afterAction(exchange);
			} catch (ClientException ex) {
				onClientError(req, ex);

				resp.setStatus(ex.getHttpsStatus());
				Integer appStatus = ex.getAppStatus();
				resp.getWriter().printf(
						"%s%s",
						appStatus == null ? "" : String.format("%d:", appStatus),
						ex.getMessage()
				);
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
