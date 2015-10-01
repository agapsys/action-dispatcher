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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class JpaTransactionServlet extends ActionServlet {
	// CLASS SCOPE =============================================================
	private static final String ATTR_ENTITY_MANAGER = "com.agapsys.web.action.dispatcher.entityManager";
	private static final String ATTR_TRANSACTION    = "com.agapsys.web.action.dispatcher.transaction";
	private static final String ATTR_RUNNABLE_QUEUE = "com.agapsys.web.action.dispatcher.runnableQueue";
	
	private static class ServletJpaTransaction extends WrappedEntityTransaction {
		private final UnsupportedOperationException exception = new UnsupportedOperationException();
		
		public ServletJpaTransaction(EntityTransaction wrappedTransaction) {
			super(wrappedTransaction);
		}
		
		@Override
		public void commit() {
			throw exception;
		}
		public void wrappedCommit() {
			super.commit();
		}
		
		@Override
		public void begin() {
			throw exception;
		}
		public void wrappedBegin() {
			super.begin();
		}
	}
	
	private static class ServletJpaEntityManger extends WrappedEntityManager {
		private final UnsupportedOperationException exception = new UnsupportedOperationException();

		private ServletJpaTransaction singleTransaction = null;
		
		public ServletJpaEntityManger(EntityManager wrappedEntityManager) {
			super(wrappedEntityManager);
		}

		@Override
		public EntityTransaction getTransaction() {
			if (singleTransaction == null) {
				singleTransaction = new ServletJpaTransaction(super.getTransaction());
			}
			return singleTransaction;
		}

		@Override
		public void close() {
			throw exception;
		}
		public void wrappedClose() {
			super.close();
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private void closeTransaction(Throwable t, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletJpaEntityManger entityManager = (ServletJpaEntityManger) req.getAttribute(ATTR_ENTITY_MANAGER);
		ServletJpaTransaction transaction = (ServletJpaTransaction) req.getAttribute(ATTR_TRANSACTION);
		
		if (transaction != null && transaction.isActive()) {
			if (t != null) {
				transaction.rollback();
			} else {
				transaction.wrappedCommit();
			}
			req.removeAttribute(ATTR_TRANSACTION);
		}
		
		if (entityManager != null) {
			entityManager.wrappedClose();
			req.removeAttribute(ATTR_ENTITY_MANAGER);
		}
	}

	@Override
	protected void onError(Throwable throwable, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		closeTransaction(throwable, req, resp);
		super.onError(throwable, req, resp);
	}
	
	@Override
	protected void afterAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.afterAction(req, resp);
		closeTransaction(null, req, resp);
		processQueue(req);
	}
	
	public EntityManager getEntityManager(HttpServletRequest req) {
		ServletJpaEntityManger em = (ServletJpaEntityManger) req.getAttribute(ATTR_ENTITY_MANAGER);
		
		if (em == null) {
			em = new ServletJpaEntityManger(getApplicationEntityManagerFactory().getEntityManager());
			
			ServletJpaTransaction transaction = (ServletJpaTransaction) em.getTransaction();
			transaction.wrappedBegin();
			
			req.setAttribute(ATTR_ENTITY_MANAGER, em);
			req.setAttribute(ATTR_TRANSACTION, transaction);
		}
		
		return em;
	}
	
	protected abstract ApplicationEntityManagerFactory getApplicationEntityManagerFactory();
	
	/**
	 * Queue given runnable to be processed after transaction is committed.
	 * @param req HTTP request
	 * @param runnable runnable to be executed
	 */
	public void invokeLater(HttpServletRequest req, Runnable runnable) {
		if (runnable == null)
			throw new IllegalArgumentException("Null runnable");
		
		List<Runnable> queue = (List<Runnable>) req.getAttribute(ATTR_RUNNABLE_QUEUE);
		if (queue == null) {
			queue = new LinkedList<>();
			req.setAttribute(ATTR_RUNNABLE_QUEUE, queue);
		}
		
		queue.add(runnable);
	}
	
	private void processQueue(HttpServletRequest req) {
		List<Runnable> queue = (List<Runnable>) req.getAttribute(ATTR_RUNNABLE_QUEUE);
		if (queue != null) {
			for (Runnable runnable : queue) {
				runnable.run();
			}
		}
	}
	// =========================================================================
}