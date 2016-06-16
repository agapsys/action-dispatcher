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

import com.agapsys.rcf.HttpExchange;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rcf.integration.ControllerGeneralTest;

@WebController // <-- default mapping will be "phase"
public class PhaseController extends PublicController {

	@Override
	public void beforeAction(HttpExchange exchange) throws ServletException, IOException {
		exchange.getResponse().setHeader(ControllerGeneralTest.PHASE_BEFORE_HEADER, ControllerGeneralTest.PHASE_BEFORE_HEADER);
	}

	@Override
	public void afterAction(HttpExchange exchange) throws ServletException, IOException {
		exchange.getResponse().setHeader(ControllerGeneralTest.PHASE_AFTER_HEADER, ControllerGeneralTest.PHASE_AFTER_HEADER);
	}

	@Override
	public void onNotFound(HttpExchange exchange) throws ServletException, IOException {
		exchange.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
		exchange.getResponse().setHeader(ControllerGeneralTest.PHASE_NOT_FOUND_HEADER, ControllerGeneralTest.PHASE_NOT_FOUND_HEADER);
	}

	@WebAction(httpMethods = HttpMethod.GET, defaultAction = true)
	public String get(HttpServletRequest req) {
		return ControllerGeneralTest.PHASE_DEFAULT_URL;
	}

	@WebAction(httpMethods = HttpMethod.POST, defaultAction = true)
	public String post(HttpServletRequest req){
		return ControllerGeneralTest.PHASE_DEFAULT_URL;
	}
}
