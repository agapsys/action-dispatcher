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
package com.agapsys.rcf;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

public class JsonResponse extends HttpResponse {

    public JsonResponse(HttpResponse wrappedResponse) {
        super(wrappedResponse);
    }

    /**
     * Sends an object in the response.
     *
     * @param object object to be serialized.
     * @throws IOException if an I/O error happened during the process.
     * @return this
     */
    public final JsonResponse sendObject(Object object) throws IOException {
        HttpServletResponse resp = _getServletResponse();
        resp.setContentType(JsonRequest.JSON_CONTENT_TYPE);
        resp.setCharacterEncoding(JsonRequest.JSON_ENCODING);

        PrintWriter out = resp.getWriter();
        out.write(JsonRequest.DEFAULT_GSON.toJson(object));
        return this;
    }

}
