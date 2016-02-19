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

package rcf.integration.controllers;

import com.agapsys.rcf.Controller;
import com.agapsys.rcf.HttpExchange;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebActions;
import com.agapsys.rcf.WebController;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import rcf.integration.ControllerGeneralTest;

@WebController("public")
public class PublicController extends Controller {

	protected void processRequest(String msg, HttpExchange exchange){
		exchange.getResponse().setStatus(HttpServletResponse.SC_OK);
		try {
			exchange.getResponse().getWriter().print(msg);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@WebAction
	public void get(HttpExchange exchange){
		processRequest(ControllerGeneralTest.PUBLIC_GET_URL, exchange);
	}

	@WebAction(mapping = "mapped/get")
	public void mappedGet(HttpExchange exchange){
		processRequest(ControllerGeneralTest.PUBLIC_MAPPED_GET_URL, exchange);
	}

	@WebAction(mapping = "/mapped/get2")
	public void mappedWithSlash(HttpExchange exchange){
		processRequest(ControllerGeneralTest.PUBLIC_MAPPED_WITH_SLASH_GET_URL, exchange);
	}

	@WebAction(httpMethods = HttpMethod.POST)
	public void post(HttpExchange exchange){
		processRequest(ControllerGeneralTest.PUBLIC_POST_URL, exchange);
	}

	@WebAction(httpMethods = HttpMethod.POST, mapping = "mapped/post")
	public void mappedPost(HttpExchange exchange){
		processRequest(ControllerGeneralTest.PUBLIC_MAPPED_POST_URL, exchange);
	}

	@WebActions({@WebAction(httpMethods = HttpMethod.GET),@WebAction(httpMethods = HttpMethod.POST)})
	public void repeatableGetOrPost(HttpExchange exchange){
		processRequest(ControllerGeneralTest.PUBLIC_WEBACTIONS_URL + exchange.getRequest().getMethod(), exchange);
	}

	@WebAction(httpMethods = {HttpMethod.GET, HttpMethod.POST})
	public void multipleMethods(HttpExchange exchange){
		processRequest(ControllerGeneralTest.PUBLIC_MULTIPLE_METHODS_URL + exchange.getRequest().getMethod(), exchange);
	}
}
