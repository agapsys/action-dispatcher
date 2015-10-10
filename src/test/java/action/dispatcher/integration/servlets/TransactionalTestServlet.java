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

package action.dispatcher.integration.servlets;

import com.agapsys.jpa.PersistenceUnit;
import com.agapsys.web.action.dispatcher.EntityManagerFactory;
import com.agapsys.web.action.dispatcher.TransactionalServlet;
import action.dispatcher.integration.jpa.PersistenceUnitFactory;
import com.agapsys.web.action.dispatcher.HttpExchange;
import com.agapsys.web.action.dispatcher.Transaction;
import com.agapsys.web.action.dispatcher.WebAction;
import action.dispatcher.integration.jpa.TestEntity;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.servlet.annotation.WebServlet;

@WebServlet("/jpa/*")
public class TransactionalTestServlet extends TransactionalServlet {
	// CLASS SCOPE =============================================================
	public static boolean postCommitted = false;
	public static boolean postRollbacked = false;
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================	
	@Override
	protected EntityManagerFactory getEntityManagerFactory() {
		final PersistenceUnit pu = PersistenceUnitFactory.getInstance();
		return new EntityManagerFactory() {

			@Override
			public EntityManager getEntityManager() {
				return pu.getEntityManager();
			}
		};
	}

	private void createEntities(Transaction rt, boolean throwError) {
		for(int i = 1; i <= 100; i++) {
			if (i == 50 && throwError)
				throw new RuntimeException();
			
			TestEntity user = new TestEntity();
			rt.getEntityManager().persist(user);
		}
	}

	@Override
	protected void beforeAction(HttpExchange exchange){
		postCommitted = false;
		postRollbacked = false;
	}
	
	private final Runnable postCommitRunnable = new Runnable() {

		@Override
		public void run() {
			postCommitted = true;
		}
	};
	
	private final Runnable postRollbackRunnable = new Runnable() {
		@Override
		public void run() {
			postRollbacked = true;
		}
	};
	
	
	@WebAction
	public void commit(HttpExchange exchange) {
		Transaction rt = getTransaction(exchange);
		rt.invokeAfterRollback(postRollbackRunnable);
		createEntities(rt, false);
		rt.invokeAfterCommit(postCommitRunnable);
	}
	
	@WebAction
	public void rollback(HttpExchange exchange) {
		Transaction rt = getTransaction(exchange);
		rt.invokeAfterRollback(postRollbackRunnable);
		createEntities(rt, true);
		rt.invokeAfterCommit(postCommitRunnable);
	}
	
	@WebAction
	public void clear(HttpExchange exchange) {
		getTransaction(exchange).getEntityManager().createQuery("delete from TestEntity t").executeUpdate();
	}
	
	@WebAction
	public void count(HttpExchange exchange) {
		Transaction rt = getTransaction(exchange);
		Long count = (Long) rt.getEntityManager().createQuery("select count(1) from TestEntity t)").getSingleResult();
		try {
			exchange.getResponse().getWriter().print("" + count);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
