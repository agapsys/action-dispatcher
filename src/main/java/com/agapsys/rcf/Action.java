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

package com.agapsys.rcf;

import java.io.IOException;
import javax.servlet.ServletException;

/**
 * Represents an action to be processed by application
 */
public interface Action {

    /**
     * Processes the action.
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if the HTTP request cannot be handled.
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request.
     */
    public void processRequest(ActionRequest request, ActionResponse response) throws ServletException, IOException;
    
}
