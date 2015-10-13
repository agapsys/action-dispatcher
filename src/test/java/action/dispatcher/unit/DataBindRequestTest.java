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

package action.dispatcher.unit;

import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.HttpPost;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.sevlet.test.StacktraceErrorHandler;
import com.agapsys.web.action.dispatcher.DataBindController;
import com.agapsys.web.action.dispatcher.DataBindRequest;
import com.agapsys.web.action.dispatcher.DataBindService;
import com.agapsys.web.action.dispatcher.DataBindServlet;
import com.agapsys.web.action.dispatcher.HttpExchange;
import com.agapsys.web.action.dispatcher.HttpMethod;
import com.agapsys.web.action.dispatcher.ObjectSerializer;
import com.agapsys.web.action.dispatcher.WebAction;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataBindRequestTest {
	
	// CLASS SCOPE =============================================================
	@WebServlet("/*")
	public static class TestServlet extends DataBindServlet {
		// CLASS SCOPE =============================================================
		private static class GsonSerializer implements ObjectSerializer {
			// CLASS SCOPE =========================================================
			public static final String JSON_CONTENT_TYPE = "application/json";
			public static final String JSON_ENCODING     = "UTF-8";

			// Check if given request is valid for GSON parsing
			private static void checkJsonContentType(HttpExchange exchange) throws ObjectSerializer.BadRequestException {
				String reqContentType = exchange.getRequest().getContentType();

				if(!reqContentType.startsWith(JSON_CONTENT_TYPE))
					throw new ObjectSerializer.BadRequestException("Invalid content-type: " + reqContentType);
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
			public <T> T readObject(HttpExchange exchange, Class<T> targetClass) throws ObjectSerializer.BadRequestException {
				if (targetClass == null)
					throw new IllegalArgumentException("Null targetClass");

				checkJsonContentType(exchange);

				try {
					return gson.fromJson(exchange.getRequest().getReader(), targetClass);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				} catch (Throwable ex) {
					throw new BadRequestException(ex);
				}
			}

			@Override
			public void writeObject(HttpExchange exchange, Object object) {
				exchange.getResponse().setContentType(JSON_CONTENT_TYPE);
				exchange.getResponse().setCharacterEncoding(JSON_ENCODING);

				PrintWriter out;
				try {
					out = exchange.getResponse().getWriter();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				String json = gson.toJson(object);
				out.write(json);
			}
		}

		private static class DataBindTestController extends DataBindController {

			public DataBindTestController(DataBindService service) {
				super(service);
			}

			@Override
			protected ObjectSerializer _getSerializer() {
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
		protected DataBindController _getController() {
			return new DataBindTestController(this);
		}

		// Actions -----------------------------------------------------------------
		@WebAction(httpMethods = HttpMethod.POST)
		@DataBindRequest(targetClass = RequestObject.class)
		public void post(HttpExchange exchange) {
			RequestObject reqObj = (RequestObject) readObject(exchange);
			ResponseObject respObj = new ResponseObject(reqObj);
			writeObject(exchange, respObj);
		}
		
		@WebAction(httpMethods = HttpMethod.GET)
		@DataBindRequest(targetClass = RequestObject.class, throwIfNonEntityEnclosed = true)
		public void getError(HttpExchange exchange) {}
		
		@WebAction(httpMethods = HttpMethod.GET)
		@DataBindRequest(targetClass = RequestObject.class, throwIfNonEntityEnclosed = false)
		public void getIgnored(HttpExchange exchange) {}
		// -------------------------------------------------------------------------
		// =========================================================================
	}
	// =========================================================================
	
	private ServletContainer sc;
	
	@Before
	public void before() {
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerServlet(TestServlet.class);
		context.setErrorHandler(new StacktraceErrorHandler());
		
		sc.registerContext(context);
		sc.startServer();
	}
	
	@After
	public void after() {
		sc.stopServer();
	}
	
	@Test
	public void sendValidObject() {
		String validJson = "{\"key\":1,\"value\":\"value\"}";
		HttpPost post = new HttpPost(sc, "/post");
		post.setContentType(ContentType.APPLICATION_JSON);
		post.setContentBody(validJson);
		HttpResponse resp = sc.doPost(post);
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
		Assert.assertEquals(validJson, resp.getResponseBody());
	}
	
	@Test
	public void sendInvalidObject() {
		String invalidJson = "{\"key\":\"key\",\"value\":\"value\"}";
		HttpPost post = new HttpPost(sc, "/post");
		post.setContentType(ContentType.APPLICATION_JSON);
		post.setContentBody(invalidJson);
		HttpResponse resp = sc.doPost(post);
		System.out.println(resp.getResponseBody());
		Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatusCode());
	}
	
	@Test
	public void testNotIgnoringInvalidRequest() {
		HttpResponse resp = sc.doGet("/getError");
		System.out.println(resp.getResponseBody());
		Assert.assertTrue(resp.getResponseBody().contains("non-entity-enclosed request (GET)"));
		Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp.getStatusCode());
	}
	
	@Test
	public void testIgnoringInvalidRequest() {
		HttpResponse resp = sc.doGet("/getIgnored");
		System.out.println(resp.getResponseBody());
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
	}
}
