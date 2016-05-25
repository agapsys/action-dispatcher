/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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
package rcf;

import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.HttpExchange;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.sevlet.container.StacktraceErrorHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
@WebController("dto")
public class DtoControllerTest extends Controller {
	private static final int SOURCE_VAL = 6;

	public static class SourceObject {
		public final int srcVal;

		public SourceObject(int val) {
			this.srcVal = val;
		}
	}

	public static class DoubleDto {
		public final int dtoVal;

		public DoubleDto(SourceObject obj) {
			this.dtoVal = obj.srcVal * 2;
		}
	}

	@WebAction(mapping = "get")
	public SourceObject getAction(HttpExchange exchange) {
		return new SourceObject(SOURCE_VAL);
	}

	@Override
	protected Object getDtoObject(Object src) {
		return new DoubleDto((SourceObject) src);
	}

	// Test code ---------------------------------------------------------------
	private ServletContainer sc;

	@Before
	public void before() {
		// Register controllers directly...
		sc = new ServletContainerBuilder()
			.registerController(DtoControllerTest.class)
			.setErrorHandler(new StacktraceErrorHandler())
			.build();

		sc.startServer();
	}

	@After
	public void after() {
		sc.stopServer();
	}

	@Test
	public void testDto() {
		HttpResponse.StringResponse resp = sc.doRequest(new HttpGet("/dto/get"));
		Assert.assertEquals(200, resp.getStatusCode());
		Assert.assertEquals(String.format("{\"dtoVal\":%s}", SOURCE_VAL * 2), resp.getContentString());
	}

}
