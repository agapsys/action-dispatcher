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
import com.agapsys.rcf.HttpRequest;
import com.agapsys.rcf.HttpResponse;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.exceptions.ClientException;
import com.agapsys.rcf.integration.ControllerGeneralTest;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebController // <-- default mapping will be "phase"
public class PhaseController extends PublicController {

    @Override
    protected void beforeAction(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        response.setHeader(ControllerGeneralTest.PHASE_BEFORE_HEADER, ControllerGeneralTest.PHASE_BEFORE_HEADER);
    }

    @Override
    protected void afterAction(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        response.setHeader(ControllerGeneralTest.PHASE_AFTER_HEADER, ControllerGeneralTest.PHASE_AFTER_HEADER);
    }

    @Override
    protected void onClientError(HttpRequest request, HttpResponse response, ClientException error) throws ServletException, IOException {
        if (error.getHttpStatus() == HttpServletResponse.SC_NOT_FOUND) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setHeader(ControllerGeneralTest.PHASE_NOT_FOUND_HEADER, ControllerGeneralTest.PHASE_NOT_FOUND_HEADER);
        }
    }

    @WebAction(httpMethods = HttpMethod.GET, defaultAction = true)
    public String get(HttpServletRequest req) {
        return ControllerGeneralTest.PHASE_DEFAULT_URL;
    }

    @WebAction(httpMethods = HttpMethod.POST, defaultAction = true)
    public String post(HttpServletRequest req){
        return ControllerGeneralTest.PHASE_DEFAULT_URL;
    }
}
