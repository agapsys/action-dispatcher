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

import com.agapsys.rcf.Controller;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.ActionRequest;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebActions;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.integration.ControllerGeneralTest;

@WebController // <-- default mapping will be "public"
public class PublicController extends Controller {

    @WebAction
    public String get(ActionRequest req) {
        return ControllerGeneralTest.PUBLIC_GET_URL;
    }

    @WebAction(mapping = "/mappedGet")
    public String mappedGet(ActionRequest exchange) {
        return ControllerGeneralTest.PUBLIC_MAPPED_GET_URL;
    }

    @WebAction(mapping = "/mappedGet2")
    public String mappedWithSlash(ActionRequest req) {
        return ControllerGeneralTest.PUBLIC_MAPPED_WITH_SLASH_GET_URL;
    }

    @WebAction(httpMethods = HttpMethod.POST)
    public String post(ActionRequest req) {
        return ControllerGeneralTest.PUBLIC_POST_URL;
    }

    @WebAction(httpMethods = HttpMethod.POST, mapping = "/mappedPost")
    public String mappedPost(ActionRequest req) {
        return ControllerGeneralTest.PUBLIC_MAPPED_POST_URL;
    }

    @WebActions({@WebAction(httpMethods = HttpMethod.GET),@WebAction(httpMethods = HttpMethod.POST)})
    public String repeatableGetOrPost(ActionRequest req) {
        return ControllerGeneralTest.PUBLIC_WEBACTIONS_URL + req.getMethod();
    }

    @WebAction(httpMethods = {HttpMethod.GET, HttpMethod.POST})
    public String multipleMethods(ActionRequest req) {
        return ControllerGeneralTest.PUBLIC_MULTIPLE_METHODS_URL + req.getMethod();
    }
}
