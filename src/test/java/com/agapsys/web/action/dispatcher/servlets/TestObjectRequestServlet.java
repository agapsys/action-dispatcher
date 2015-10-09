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
import com.agapsys.web.action.dispatcher.HttpMethod;
import com.agapsys.web.action.dispatcher.ObjectRequest;
import com.agapsys.web.action.dispatcher.ObjectRequestController;
import com.agapsys.web.action.dispatcher.ObjectRequestServlet;
import com.agapsys.web.action.dispatcher.ObjectSerializer;
import com.agapsys.web.action.dispatcher.RequestResponsePair;
import com.agapsys.web.action.dispatcher.WebAction;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;

@WebServlet("/*")
public class TestObjectRequestServlet extends ObjectRequestServlet {
	// CLASS SCOPE =============================================================
	private static class GsonSerializer implements ObjectSerializer {
		// CLASS SCOPE =========================================================
		public static final String JSON_CONTENT_TYPE = "application/json";
		public static final String JSON_ENCODING     = "UTF-8";

		// Check if given request is valid for GSON parsing
		private static void checkJsonContentType(RequestResponsePair rrp) throws BadRequestException {
			String reqContentType = rrp.getRequest().getContentType();

			if(!reqContentType.startsWith(JSON_CONTENT_TYPE))
				throw new BadRequestException("Invalid content-type: " + reqContentType);
		}
		// =====================================================================
		
		// INSTANCE SCOPE ======================================================
		private final Gson gson;
		
		public GsonSerializer() {
			this.gson = new Gson();
		}
		
		public GsonSerializer(Gson gson) {
			if (gson == null)
				throw new IllegalArgumentException("Null gson");
			
			this.gson = gson;
		}
		
		@Override
		public <T> T getObject(RequestResponsePair rrp, Class<T> targetClass) throws BadRequestException {
			if (targetClass == null)
				throw new IllegalArgumentException("Null targetClass");
		
			checkJsonContentType(rrp);

			try {
				return gson.fromJson(rrp.getRequest().getReader(), targetClass);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public void sendObject(RequestResponsePair rrp, Object object) {
			rrp.getResponse().setContentType(JSON_CONTENT_TYPE);
			rrp.getResponse().setCharacterEncoding(JSON_ENCODING);

			PrintWriter out;
			try {
				out = rrp.getResponse().getWriter();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			String json = gson.toJson(object);
			out.write(json);
		}
	}
	
	private static class TestObjectRequestController extends ObjectRequestController {

		public TestObjectRequestController(ActionServlet servlet) {
			super(servlet);
		}

		@Override
		protected ObjectSerializer getSerializer() {
			return new GsonSerializer();
		}
	}
	
	public static class RequestObject {
		public int key;
		public String value;
	}
	
	public static class ResponseObject extends RequestObject {
		public ResponseObject(RequestObject reqObj) {
			this.key = reqObj.key;
			this.value = reqObj.value;
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	@Override
	protected ObjectRequestController getController() {
		return new TestObjectRequestController(this);
	}
	
	// Actions -----------------------------------------------------------------
	@WebAction(httpMethod = HttpMethod.POST)
	@ObjectRequest(targetClass = RequestObject.class)
	public void post(RequestResponsePair rrp) {
		RequestObject reqObj = (RequestObject) getObject(rrp);
		ResponseObject respObj = new ResponseObject(reqObj);
		sendObject(rrp, respObj);
	}
	// -------------------------------------------------------------------------
	// =========================================================================
}
