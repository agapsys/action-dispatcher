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
 * Utility class to lazy-initialize objects in a multi-thread environment
 * @author Leandro Oliveira (leandro@agapsys.com)
 * @param <T> Type of the object managed by this initializer
 */
public abstract class LazyInitializer<T> {
	private volatile boolean initialized = false;
	private T instance;

	/** Called during initialization. Default implementation does nothing. */
	protected void onInitialize() {}
	
	/** 
	 * Returns the object associated with this initializer.
	 * @return the object associated with this initializer.
	 * Default implementation returns null. This method will be called only once
	 */
	protected T getLazyInstance() {
		return null;
	}
	
	/** Starts the initializer. */
	public final synchronized void initialize() {
		if (!initialized) {
			onInitialize();
			instance = getLazyInstance();
			initialized = true;
		}
	}
	
	/**
	 * Returns a boolean indicating if instance was initialized.
	 * @return initialized state
	 */
	public final boolean isInitialized() {
		return initialized;
	}
	
	/** 
	 * Return an instance associated with this initializer.
	 * Returned instance will be obtained via {@linkplain LazyInitializer#getLazyInstance(Object...)}.
	 * @return the instance associated with this initializer.
	 */
	public final T getInstance() {
		if (!initialized) {
			initialize();
		}
		return instance;
	}
}
