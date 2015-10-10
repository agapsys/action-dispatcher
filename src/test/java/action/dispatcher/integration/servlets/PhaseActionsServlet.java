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

import com.agapsys.web.action.dispatcher.HttpMethod;
import com.agapsys.web.action.dispatcher.HttpExchange;
import com.agapsys.web.action.dispatcher.WebAction;
import action.dispatcher.integration.ActionServletGeneralTest;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/phase/*")
public class PhaseActionsServlet extends PublicServlet {
	
	@Override
	public void beforeAction(HttpExchange exchange){
		exchange.getResponse().setHeader(ActionServletGeneralTest.PHASE_BEFORE_HEADER, ActionServletGeneralTest.PHASE_BEFORE_HEADER);
	}
	
	@Override
	public void afterAction(HttpExchange exchange){
		exchange.getResponse().setHeader(ActionServletGeneralTest.PHASE_AFTER_HEADER, ActionServletGeneralTest.PHASE_AFTER_HEADER);
	}
	
	@Override
	public void onNotFound(HttpExchange exchange){
		exchange.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
		exchange.getResponse().setHeader(ActionServletGeneralTest.PHASE_NOT_FOUND_HEADER, ActionServletGeneralTest.PHASE_NOT_FOUND_HEADER);
	}
	
	@Override
	@WebAction(httpMethod = HttpMethod.GET, defaultAction = true)
	public void get(HttpExchange exchange) {
		processRequest(ActionServletGeneralTest.PHASE_DEFAULT_URL, exchange);
	}
	
	@Override
	@WebAction(httpMethod = HttpMethod.POST, defaultAction = true)
	public void post(HttpExchange exchange){
		processRequest(ActionServletGeneralTest.PHASE_DEFAULT_URL, exchange);
	}
}
