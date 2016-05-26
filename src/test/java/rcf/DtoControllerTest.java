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
import com.agapsys.rcf.Dto;
import com.agapsys.rcf.HttpExchange;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.sevlet.container.StacktraceErrorHandler;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	@Dto(DoubleDto.class)
	public static class SourceObject {
		public final int srcVal;

		public SourceObject(int val) {
			this.srcVal = val;
		}

		@Override
		public String toString() {
			return String.format("srcVal:%d", srcVal);
		}


	}

	public static class DoubleDto {
		public final int dtoVal;

		public DoubleDto(SourceObject obj) {
			this.dtoVal = obj.srcVal * 2;
		}

		@Override
		public String toString() {
			return String.format("dtoVal:%d", dtoVal);
		}
	}

	@WebAction
	public SourceObject getObject(HttpExchange exchange) {
		return new SourceObject(1);
	}

	@WebAction
	public List<SourceObject> getList(HttpExchange exchange) {
		List<SourceObject> list = new LinkedList<>();
		list.add(new SourceObject(0));
		list.add(new SourceObject(1));
		list.add(new SourceObject(2));

		return list;
	}

	@WebAction
	public Set<SourceObject> getSet(HttpExchange exchange) {
		Set<SourceObject> set = new LinkedHashSet<>();
		set.add(new SourceObject(3));
		set.add(new SourceObject(4));
		set.add(new SourceObject(5));
		return set;
	}

	@WebAction
	public Map<Object, SourceObject> getMap(HttpExchange exchange) {
		Map<Object, SourceObject> map = new LinkedHashMap<>();
		map.put("a", new SourceObject(1));
		map.put("b", new SourceObject(3));
		map.put("c", new SourceObject(5));
		return map;
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
	public void testGetObject() {
		HttpResponse.StringResponse resp = sc.doRequest(new HttpGet("/dto/getObject"));
		Assert.assertEquals(200, resp.getStatusCode());
		Assert.assertEquals(String.format("{\"dtoVal\":%s}", 2), resp.getContentString());
	}

	@Test
	public void testGetList() {
		HttpResponse.StringResponse resp = sc.doRequest(new HttpGet("/dto/getList"));
		Assert.assertEquals(200, resp.getStatusCode());
		Assert.assertEquals(String.format("[{\"dtoVal\":%s},{\"dtoVal\":%s},{\"dtoVal\":%s}]", 0, 2, 4), resp.getContentString());
	}

	@Test
	public void testGetSet() {
		HttpResponse.StringResponse resp = sc.doRequest(new HttpGet("/dto/getSet"));
		Assert.assertEquals(200, resp.getStatusCode());
		Assert.assertEquals(String.format("[{\"dtoVal\":%s},{\"dtoVal\":%s},{\"dtoVal\":%s}]", 6, 8, 10), resp.getContentString());
	}

	@Test
	public void testGetMap() {
		HttpResponse.StringResponse resp = sc.doRequest(new HttpGet("/dto/getMap"));
		Assert.assertEquals(200, resp.getStatusCode());
		Assert.assertEquals(String.format("{\"a\":{\"dtoVal\":%s},\"b\":{\"dtoVal\":%s},\"c\":{\"dtoVal\":%s}}", 2, 6, 10), resp.getContentString());
	}

}
