/*
 * Copyright 2017 Agapsys Tecnologia Ltda-ME.
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
import com.agapsys.rcf.ActionRequest;
import com.agapsys.rcf.ActionResponse;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.integration.AppUser;
import java.io.IOException;
import javax.servlet.ServletException;

@WebController("secured")
public class SecuredController extends Controller {

    public static final String ROLE       = "role";
    public static final String PARAM_ROLE = "role";

    @WebAction(secured = true)
    public void securedGet() {}

    @WebAction(requiredRoles = {ROLE})
    public void securedGetWithRoles() {}

    @WebAction
    public void logUser(ActionRequest request, ActionResponse response) throws ServletException, IOException {
        registerUser(request, response, new AppUser(request.getOptionalParameter(PARAM_ROLE, "")));
    }

    // NOTE the order of parameters is inverted intentionally to ensure controller's MethodActionDispatcher is invoked correctly.
    @WebAction
    public void unlogUser(ActionResponse response, ActionRequest request) throws ServletException, IOException {
        registerUser(request, response, null);
    }

}
