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

import com.agapsys.rcf.Controller;
import com.agapsys.rcf.WebController;

/**
 *	Utility class to generate container with controllers
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ServletContainerBuilder extends com.agapsys.sevlet.container.ServletContainerBuilder {

	public ServletContainerBuilder registerController(Class<? extends Controller> controller, String name) {
		return (ServletContainerBuilder) super.registerServlet(controller, String.format("/%s/*", name));
	}
	
	public ServletContainerBuilder registerController(Class<? extends Controller> controller) {
		WebController[] annotations = controller.getAnnotationsByType(WebController.class);
		
		if (annotations.length == 0)
			throw new IllegalArgumentException("Controller class does not have a WebController annotation");

		for (WebController annotation : annotations) {
			String name = annotation.value();
			if (name == null || name.trim().isEmpty())
				name = controller.getSimpleName();
			
			registerController(controller, name);
		}
	
		return this;
	}
	 
}
