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

/**
 * Represents an action service.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public interface ActionService {
	/** 
	 * Called before an action. 
	 * @param exchange HTTP exchange
	 */
	public void beforeAction(HttpExchange exchange);
	
	/** 
	 * Called after an action. 
	 * @param exchange HTTP exchange
	 */
	public void afterAction(HttpExchange exchange);
	
	/** 
	 * Called when an action is not found.
	 * @param exchange HTTP exchange
	 */
	public void onNotFound(HttpExchange exchange);
	
	/** 
	 * Handles an error in the application and returns a boolean indicating if error shall be propagated.
	 * @param exchange HTTP exchange
	 * @param throwable error
	 * @return a boolean indicating if given error shall be propagated.
	 */
	public boolean onError(HttpExchange exchange, Throwable throwable);
	
	/**
	 * Called when an action is not allowed to be executed.
	 * @param exchange HTTP exchange
	 */
	public void onNotAllowed(HttpExchange exchange);
}
