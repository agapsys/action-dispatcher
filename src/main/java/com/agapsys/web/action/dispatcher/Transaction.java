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

import javax.persistence.EntityManager;

/** 
 * Represents a transaction managed by a {@linkplain TransactionalServlet}
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public interface Transaction {
	/**
	 * Returns entity manager associated with this transaction.
	 * Multiple calls to this method will return the same instance.
	 * @return the entity manager associated with this transaction
	 */
	public EntityManager getEntityManager();
	
	/**
	 * Queues given runnable to be executed after transaction commit.
	 * If this transaction is not committed, given runnable will not be executed.
	 * @param runnable runnable to be queued
	 */
	public void invokeAfterCommit(Runnable runnable);
	
	/**
	 * Queues given runnable to be executed after transaction rollback.
	 * If this transaction is not rolled back, given runnable will not be executed.
	 * @param runnable runnable to be queued
	 */
	public void invokeAfterRollback(Runnable runnable);
	
	/**
	 * Returns the HTTP exchange related to this transaction
	 * @return the HTTP exchange related to this transaction
	 */
	public HttpExchange getHttpExchange();
}
