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

import action.dispatcher.integration.ActionServletGeneralTest;
import com.agapsys.web.action.dispatcher.HttpExchange;
import com.agapsys.web.action.dispatcher.HttpMethod;
import com.agapsys.web.action.dispatcher.WebAction;
import javax.servlet.annotation.WebServlet;

@WebServlet("/default/*")
public class DefaultActionServlet extends PublicServlet {
	@Override
	@WebAction(httpMethods = HttpMethod.GET, defaultAction = true)
	public void get(HttpExchange exchange) {
		processRequest(ActionServletGeneralTest.DEFAULT_ACTION_GET_URL, exchange);
	}
	
	@Override
	@WebAction(httpMethods = HttpMethod.POST, defaultAction = true)
	public void post(HttpExchange exchange) {
		processRequest(ActionServletGeneralTest.DEFAULT_ACTION_POST_URL, exchange);
	}
}
