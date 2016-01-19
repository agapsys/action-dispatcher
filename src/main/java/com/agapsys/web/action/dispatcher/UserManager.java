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
 * User manager.
 * A user manager is the object responsible by managing application users across multiple requests.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public interface UserManager {
	/**
	 * Returns a user associated with the HTTP exchange
	 * @param exchange HTTP exchange
	 * @return application user associated with given HTTP exchange or null if there is no user
	 */
	public User getUser(HttpExchange exchange);
	
	/**
	 * Register a user associated with the HTTP exchange
	 * @param exchange HTTP exchange
	 * @param user user to be registered.
	 */
	public void login(HttpExchange exchange, User user);
	
	/**
	 * logout an user associated with the HTTP exchange
	 * @param exchange HTTP exchange
	 */
	public void logout(HttpExchange exchange);
}
