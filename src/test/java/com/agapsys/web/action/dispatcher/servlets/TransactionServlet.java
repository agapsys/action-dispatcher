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

package com.agapsys.web.action.dispatcher.servlets;

import com.agapsys.jpa.PersistenceUnit;
import com.agapsys.web.action.dispatcher.ApplicationEntityManagerFactory;
import com.agapsys.web.action.dispatcher.JpaTransactionServlet;
import com.agapsys.web.action.dispatcher.PersistenceUnitFactory;
import com.agapsys.web.action.dispatcher.WebAction;
import com.agapsys.web.action.dispatcher.entities.TestEntity;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/jpa/*")
public class TransactionServlet extends JpaTransactionServlet {
	// CLASS SCOPE =============================================================
	public static boolean postCommitted = false;
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================	
	@Override
	protected ApplicationEntityManagerFactory getApplicationEntityManagerFactory() {
		final PersistenceUnit pu = PersistenceUnitFactory.getInstance();
		return new ApplicationEntityManagerFactory() {

			@Override
			public EntityManager getEntityManager() {
				return pu.getEntityManager();
			}
		};
	}

	private void createEntities(EntityManager em, boolean throwError) {
		for(int i = 1; i <= 100; i++) {
			if (i == 50 && throwError)
				throw new RuntimeException();
			
			TestEntity user = new TestEntity();
			em.persist(user);
		}
	}

	@Override
	protected void beforeAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		postCommitted = false;
	}
	
	private final Runnable postCommitRunnable = new Runnable() {

		@Override
		public void run() {
			postCommitted = true;
		}
	
	};
	
	@WebAction
	public void commit(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		EntityManager em = getEntityManager(req);
		createEntities(em, false);
		invokeLater(req, postCommitRunnable);
	}
	
	@WebAction
	public void rollback(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		EntityManager em = getEntityManager(req);
		createEntities(em, true);
		invokeLater(req, postCommitRunnable);
	}
	
	@WebAction
	public void clear(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		getEntityManager(req).createQuery("delete from TestEntity t").executeUpdate();
	}
	
	@WebAction
	public void count(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		EntityManager em = getEntityManager(req);
		Long count = (Long) em.createQuery("select count(1) from TestEntity t)").getSingleResult();
		resp.getWriter().print("" + count);
	}
}
