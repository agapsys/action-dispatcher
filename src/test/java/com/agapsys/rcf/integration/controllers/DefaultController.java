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

package com.agapsys.rcf.integration.controllers;

import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.ActionRequest;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebActions;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.integration.ControllerGeneralTest;
import javax.servlet.http.HttpServletRequest;

@WebController("defaultController")
public class DefaultController extends PublicController {

    @WebActions({
        @WebAction(httpMethods = HttpMethod.GET),
        @WebAction(httpMethods = HttpMethod.GET, mapping = "/")
    })
    public String get(ActionRequest req) {
        return ControllerGeneralTest.DEFAULT_ACTION_GET_URL;
    }


    @WebActions({
        @WebAction(httpMethods = HttpMethod.POST),
        @WebAction(httpMethods = HttpMethod.POST, mapping = "/")
    })
    public String post(HttpServletRequest req) {
        return ControllerGeneralTest.DEFAULT_ACTION_POST_URL;
    }
}
