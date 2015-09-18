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

import java.util.List;
import java.util.Map;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

class WrappedEntityManager implements EntityManager {
	private final EntityManager wrappedEntityManager;

	public WrappedEntityManager(EntityManager wrappedEntityManager) {
		if (wrappedEntityManager == null)
			throw new IllegalArgumentException("Null entityManager");
		
		this.wrappedEntityManager = wrappedEntityManager;
	}

	@Override
	public void persist(Object entity) {
		wrappedEntityManager.persist(entity);
	}

	@Override
	public <T> T merge(T entity) {
		return wrappedEntityManager.merge(entity);
	}

	@Override
	public void remove(Object entity) {
		wrappedEntityManager.remove(entity);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return wrappedEntityManager.find(entityClass, primaryKey);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		return wrappedEntityManager.find(entityClass, primaryKey, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		return wrappedEntityManager.find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		return wrappedEntityManager.find(entityClass, primaryKey, lockMode, properties);
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		return wrappedEntityManager.getReference(entityClass, primaryKey);
	}

	@Override
	public void flush() {
		wrappedEntityManager.flush();
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		wrappedEntityManager.setFlushMode(flushMode);
	}

	@Override
	public FlushModeType getFlushMode() {
		return wrappedEntityManager.getFlushMode();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {
		wrappedEntityManager.lock(entity, lockMode);
	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		wrappedEntityManager.lock(entity, lockMode, properties);
	}

	@Override
	public void refresh(Object entity) {
		wrappedEntityManager.refresh(entity);
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		wrappedEntityManager.refresh(entity, properties);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		wrappedEntityManager.refresh(entity, lockMode);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		wrappedEntityManager.refresh(entity, lockMode, properties);
	}

	@Override
	public void clear() {
		wrappedEntityManager.clear();
	}

	@Override
	public void detach(Object entity) {
		wrappedEntityManager.detach(entity);
	}

	@Override
	public boolean contains(Object entity) {
		return wrappedEntityManager.contains(entity);
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		return wrappedEntityManager.getLockMode(entity);
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		wrappedEntityManager.setProperty(propertyName, value);
	}

	@Override
	public Map<String, Object> getProperties() {
		return wrappedEntityManager.getProperties();
	}

	@Override
	public Query createQuery(String qlString) {
		return wrappedEntityManager.createQuery(qlString);
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return wrappedEntityManager.createQuery(criteriaQuery);
	}

	@Override
	public Query createQuery(CriteriaUpdate updateQuery) {
		return wrappedEntityManager.createQuery(updateQuery);
	}

	@Override
	public Query createQuery(CriteriaDelete deleteQuery) {
		return wrappedEntityManager.createQuery(deleteQuery);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return wrappedEntityManager.createQuery(qlString, resultClass);
	}

	@Override
	public Query createNamedQuery(String name) {
		return wrappedEntityManager.createNamedQuery(name);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return wrappedEntityManager.createNamedQuery(name, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		return wrappedEntityManager.createNativeQuery(sqlString);
	}

	@Override
	public Query createNativeQuery(String sqlString, Class resultClass) {
		return wrappedEntityManager.createNativeQuery(sqlString, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		return wrappedEntityManager.createNativeQuery(sqlString, resultSetMapping);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		return wrappedEntityManager.createNamedStoredProcedureQuery(name);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		return wrappedEntityManager.createStoredProcedureQuery(procedureName);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
		return wrappedEntityManager.createStoredProcedureQuery(procedureName, resultClasses);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		return wrappedEntityManager.createStoredProcedureQuery(procedureName, resultSetMappings);
	}

	@Override
	public void joinTransaction() {
		wrappedEntityManager.joinTransaction();
	}

	@Override
	public boolean isJoinedToTransaction() {
		return wrappedEntityManager.isJoinedToTransaction();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return wrappedEntityManager.unwrap(cls);
	}

	@Override
	public Object getDelegate() {
		return wrappedEntityManager.getDelegate();
	}

	@Override
	public void close() {
		wrappedEntityManager.close();
	}

	@Override
	public boolean isOpen() {
		return wrappedEntityManager.isOpen();
	}

	@Override
	public EntityTransaction getTransaction() {
		return wrappedEntityManager.getTransaction();
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return wrappedEntityManager.getEntityManagerFactory();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return wrappedEntityManager.getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel() {
		return wrappedEntityManager.getMetamodel();
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		return wrappedEntityManager.createEntityGraph(rootType);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName) {
		return wrappedEntityManager.createEntityGraph(graphName);
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName) {
		return wrappedEntityManager.getEntityGraph(graphName);
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		return wrappedEntityManager.getEntityGraphs(entityClass);
	}
}
