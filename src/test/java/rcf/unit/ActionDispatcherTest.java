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
package rcf.unit;

import com.agapsys.rcf.Action;
import com.agapsys.rcf.ActionDispatcher;
import com.agapsys.rcf.HttpExchange;
import com.agapsys.rcf.HttpMethod;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;

public class ActionDispatcherTest {
	// CLASS SCOPE =============================================================
	private static class TestAction implements Action {
		@Override
		public void processRequest(HttpExchange exchange) {
			exchange.getResponse().setStatus(HttpServletResponse.SC_OK);
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private ActionDispatcher dispatcher;
	
	@Before
	public void setUp() {
		dispatcher = new ActionDispatcher();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPassNullAction() {
		dispatcher.registerAction(null, HttpMethod.POST, "/test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPassNullMethod() {
		TestAction action = new TestAction();
		dispatcher.registerAction(action, null, "/test");
	}

	@Test
	public void testSameUrlDistinctMethods() {
		TestAction action = new TestAction();
		dispatcher.registerAction(action, HttpMethod.GET, "/test");
		dispatcher.registerAction(action, HttpMethod.POST, "/test");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testSameUrlSameMethod() {
		TestAction action = new TestAction();
		dispatcher.registerAction(action, HttpMethod.GET, "/test");
		dispatcher.registerAction(action, HttpMethod.GET, "/test");
	}
	// =========================================================================
}
