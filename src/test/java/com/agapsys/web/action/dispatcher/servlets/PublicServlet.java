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

package com.agapsys.web.action.dispatcher.servlets;

import com.agapsys.web.action.dispatcher.ActionServlet;
import com.agapsys.web.action.dispatcher.ActionServletGeneralTest;
import com.agapsys.web.action.dispatcher.HttpMethod;
import com.agapsys.web.action.dispatcher.RequestResponsePair;
import com.agapsys.web.action.dispatcher.WebAction;
import com.agapsys.web.action.dispatcher.WebActions;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/public/*")
public class PublicServlet extends ActionServlet {
	
	protected void processRequest(String msg, RequestResponsePair rrp){
		rrp.getResponse().setStatus(HttpServletResponse.SC_OK);
		try {
			rrp.getResponse().getWriter().print(msg);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@WebAction
	public void get(RequestResponsePair rrp){
		processRequest(ActionServletGeneralTest.PUBLIC_GET_URL, rrp);
	}
	
	@WebAction(mapping = "mapped/get")
	public void mappedGet(RequestResponsePair rrp){
		processRequest(ActionServletGeneralTest.PUBLIC_MAPPED_GET_URL, rrp);
	}
	
	@WebAction(mapping = "/mapped/get2")
	public void mappedWithSlash(RequestResponsePair rrp){
		processRequest(ActionServletGeneralTest.PUBLIC_MAPPED_WITH_SLASH_GET_URL, rrp);
	}
	
	
	@WebAction(httpMethod = HttpMethod.POST)
	public void post(RequestResponsePair rrp){
		processRequest(ActionServletGeneralTest.PUBLIC_POST_URL, rrp);
	}
	
	@WebAction(httpMethod = HttpMethod.POST, mapping = "mapped/post")
	public void mappedPost(RequestResponsePair rrp){
		processRequest(ActionServletGeneralTest.PUBLIC_MAPPED_POST_URL, rrp);
	}
	
	@WebActions({@WebAction(httpMethod = HttpMethod.GET),@WebAction(httpMethod = HttpMethod.POST)})
	public void repeatableGetOrPost(RequestResponsePair rrp){
		processRequest(ActionServletGeneralTest.PUBLIC_REPEATABLE_GET_POST_URL + rrp.getRequest().getMethod(), rrp);
	}
}
