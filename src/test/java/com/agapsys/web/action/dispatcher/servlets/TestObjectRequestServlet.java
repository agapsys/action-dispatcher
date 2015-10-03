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
import com.agapsys.web.action.dispatcher.WebAction;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/*")
public class TestObjectRequestServlet extends ObjectRequestServlet {
	// CLASS SCOPE =============================================================
	private static class GsonSerializer implements ObjectSerializer {
		// CLASS SCOPE =========================================================
		public static final String JSON_CONTENT_TYPE = "application/json";
		public static final String JSON_ENCODING     = "UTF-8";

		// Check if given request is valid for GSON parsing
		private static void checkJsonContentType(HttpServletRequest req) throws BadRequestException {
			String reqContentType = req.getContentType();

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
		public <T> T getObject(HttpServletRequest req, Class<T> targetClass) throws BadRequestException, IOException {
			if (targetClass == null)
				throw new IllegalArgumentException("Null targetClass");
		
			checkJsonContentType(req);

			try {
				return gson.fromJson(req.getReader(), targetClass);
			} catch (JsonIOException ex) {
				throw new IOException(ex);
			} catch (JsonSyntaxException ex) {
				throw new BadRequestException("Malformed JSON", ex);
			}
		}

		@Override
		public void sendObject(HttpServletResponse resp, Object object) throws IOException {
			resp.setContentType(JSON_CONTENT_TYPE);
			resp.setCharacterEncoding(JSON_ENCODING);

			PrintWriter out = resp.getWriter();
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
	public void post(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		RequestObject reqObj = (RequestObject) getObject(req);
		ResponseObject respObj = new ResponseObject(reqObj);
		sendObject(resp, respObj);
	}
	// -------------------------------------------------------------------------
	// =========================================================================
}
