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

package com.agapsys.web.action.dispatcher;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Specialization of {@linkplain ActionServlet} which manages JPA transactions during HTTP exchange processing.
 * A transaction will be initialized after each HTTP exchange and will be committed (when exchange is successfully processed) or rolled back (if there is an error while processing the exchange).
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class TransactionalServlet extends ActionServlet {
	// CLASS SCOPE =============================================================
	/** Name of request attribute containing the transaction. */
	private static final String REQ_ATTR_TRANSACTION = "com.agapsys.web.action.dispatcher.transaction";
	
	private static class ServletTransaction extends WrappedEntityTransaction implements Transaction {
		private final UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Transaction is managed by servlet");
		private final EntityManager em;
		private final HttpExchange exchange;
		private final List<Runnable> commitQueue = new LinkedList<>();
		private final List<Runnable> rollbackQueue = new LinkedList<>();
		
		public ServletTransaction(HttpExchange exchange, ServletEntityManger em, EntityTransaction wrappedTransaction) {
			super(wrappedTransaction);
			this.em = em;
			this.exchange = exchange;
		}
		
		private void processQueue(List<Runnable> queue) {
			for (Runnable runnable : queue) {
				runnable.run();
			}
			queue.clear();
		}
		
		@Override
		public void commit() {
			throw unsupportedOperationException;
		}
		public void wrappedCommit() {
			super.commit();
			processQueue(commitQueue);
		}
		
		@Override
		public void begin() {
			throw unsupportedOperationException;
		}
		public void wrappedBegin() {
			super.begin();
		}

		@Override
		public void rollback() {
			throw unsupportedOperationException;
		}
		public void wrappedRollback() {
			super.rollback();
			processQueue(rollbackQueue);
		}

		@Override
		public EntityManager getEntityManager() {
			return em;
		}

		private void invokeAfter(List<Runnable> queue, Runnable runnable) {
			if (runnable == null)
				throw new IllegalArgumentException("Null runnable");
			
			queue.add(runnable);
		}
		
		@Override
		public void invokeAfterCommit(Runnable runnable) {
			invokeAfter(commitQueue, runnable);
		}

		@Override
		public void invokeAfterRollback(Runnable runnable) {
			invokeAfter(rollbackQueue, runnable);
		}

		@Override
		public HttpExchange getHttpExchange() {
			return exchange;
		}
	}
	
	private static class ServletEntityManger extends WrappedEntityManager {
		private final UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Entity manager is managed by servlet");
		private final ServletTransaction singleTransaction;
		
		public ServletEntityManger(HttpExchange exchange, EntityManager wrappedEntityManager) {
			super(wrappedEntityManager);
			singleTransaction = new ServletTransaction(exchange, this, super.getTransaction());
		}

		@Override
		public EntityTransaction getTransaction() {
			return singleTransaction;
		}

		@Override
		public void close() {
			throw unsupportedOperationException;
		}
		public void wrappedClose() {
			super.close();
		}
	}
	
	private static class DefaultTransactionalHttpExchange extends DefaultHttpExchange implements TransactionalHttpExchange {
		public DefaultTransactionalHttpExchange(TransactionalServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
			super(servlet, req, resp);
		}

		@Override
		public Transaction getTransaction() {
			return ((TransactionalServlet) getServlet()).getTransaction(this);
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final LazyInitializer<EntityManagerProvider> entityManagerProvider = new LazyInitializer<EntityManagerProvider>() {

		@Override
		protected EntityManagerProvider getLazyInstance(Object...params) {
			return TransactionalServlet.this._getEntityManagerProvider();
		}
	};
	
	private void closeTransaction(HttpExchange exchange, Throwable t) {
		HttpServletRequest req = exchange.getRequest();
		ServletTransaction transaction = (ServletTransaction) req.getAttribute(REQ_ATTR_TRANSACTION);
		
		if (transaction != null) {
			if (t != null) {
				transaction.wrappedRollback();
			} else {
				transaction.wrappedCommit();
			}
			
			((ServletEntityManger)transaction.getEntityManager()).wrappedClose();
		}
	}

	// CUSTOMIZABLE INITIALIZATION BEHAVIOUR -----------------------------------
	/** 
	 * Return the factory of entity managers used by this servlet. 
	 * This method is intended to be overridden to change servlet initialization and not be called directly
	 * @return {@link EntityManagerProvider} instance used by this servlet. Default implementation returns null.
	 */
	protected EntityManagerProvider _getEntityManagerProvider() {
		return null;
	}
	// -------------------------------------------------------------------------
	
	/** 
	 * Handles an error in the application and returns a boolean indicating if error shall be propagated. Default implementation rollback current transaction.
	 * @param exchange HTTP exchange
	 * @param throwable error
	 * @return a boolean indicating if given error shall be propagated. Default always return true.
	 */
	@Override
	public boolean onError(HttpExchange exchange, Throwable throwable) {
		super.onError(exchange, throwable);
		closeTransaction(exchange, throwable);
		return true;
	}

	@Override
	public void beforeAction(HttpExchange exchange) {
		super.beforeAction(exchange);
		getTransaction(exchange); // <-- Forces transaction on each request
	}
	
	
	@Override
	public void afterAction(HttpExchange exchange) {
		closeTransaction(exchange, null);
	}
	
	@Override
	protected TransactionalHttpExchange getHttpExchange(HttpServletRequest req, HttpServletResponse resp) {
		return new DefaultTransactionalHttpExchange(this, req, resp);
	}
	
	/**
	 * Returns the transaction associated with given request.
	 * Multiple calls to this methods passing the same request will return the same transaction instance.
	 * @param exchange HTTP exchange
	 * @return the transaction associated with given request
	 */
	public final Transaction getTransaction(HttpExchange exchange) {
		HttpServletRequest req = exchange.getRequest();
		EntityManagerProvider emp = entityManagerProvider.getInstance();
		
		if (emp != null) {
			ServletTransaction transaction = (ServletTransaction) req.getAttribute(REQ_ATTR_TRANSACTION);

			if (transaction == null) {
				transaction = (ServletTransaction) new ServletEntityManger(exchange, emp.getEntityManager()).getTransaction();
				transaction.wrappedBegin();
				req.setAttribute(REQ_ATTR_TRANSACTION, transaction);
			}

			return transaction;
		} else {
			throw new UnsupportedOperationException("Servlet does not have an associated " + EntityManagerProvider.class.getName());
		}
	}
	// =========================================================================
}