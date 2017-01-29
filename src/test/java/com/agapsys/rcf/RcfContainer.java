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

package com.agapsys.rcf;

import com.agapsys.jee.TestingServletContainer;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * Utility class to generate container with controllers
 */
public class RcfContainer<RC extends RcfContainer<RC>> extends TestingServletContainer<RC> {

    public static RcfContainer<?> newInstance(Class<? extends HttpServlet>...servletsOrControllers) {
        RcfContainer rc = new RcfContainer<>();

        for (Class<? extends HttpServlet> servlet : servletsOrControllers) {

            if (Controller.class.isAssignableFrom(servlet)) {
                rc.registerController(servlet);

                if (servlet.getAnnotation(WebServlet.class) != null) {
                    rc.registerServlet(servlet);
                }

            } else {
                rc.registerServlet(servlet);
            }
        }

        return rc;
    }

    public RC registerController(Class<? extends Controller> controllerClass, String name) {
        return (RC) super.registerServlet(controllerClass, String.format("/%s/*", name));
    }

    public RC registerController(Class<? extends Controller> controllerClass) {
        WebController annotation = controllerClass.getAnnotation(WebController.class);

        if (annotation == null)
            throw new IllegalArgumentException("Controller class does not have a WebController annotation");

        String name = annotation.value().trim();

        if (name.isEmpty())
            name = ControllerRegistrationListener.getDefaultMapping(controllerClass);

        return registerController(controllerClass, name);
    }

}
