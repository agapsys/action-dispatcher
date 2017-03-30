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

import com.agapsys.rcf.ActionRequest;
import com.agapsys.rcf.ActionResponse;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.JsonRequest;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.exceptions.BadRequestException;
import java.util.List;

@WebController("json")
public class JsonController extends Controller {
    
    public static class Dto {
        public String string;
        public int integer;
    }
    
    @WebAction(httpMethods = HttpMethod.POST, mapping = "/list")
    public String list(List<String> stringList) {
        if (!stringList.get(0).equals("zero"))
            throw new RuntimeException("Invalid element at index 0");
        
        if (!stringList.get(1).equals("one"))
            throw new RuntimeException("Invalid element at index 1");
        
        return "OK";
    }
    
    @WebAction(httpMethods = HttpMethod.POST, mapping = "/wildcardList")
    public void wildcardList(List<?> stringList) {}
    
    @WebAction(httpMethods = HttpMethod.POST, mapping = "/genericList")
    public void genericList(List stringList) {}
    
    @WebAction(httpMethods = HttpMethod.POST, mapping = "/dto")
    public String postDto(Dto dto) {
        if (!dto.string.equals("string"))
            throw new BadRequestException("Invalid value for \"string\" field: " + dto.string);
        
        if (dto.integer != 12)
            throw new BadRequestException("Invalid value for \"integer\" field: " + dto.integer);
        
        return "OK";
    }
    
    @WebAction(httpMethods = HttpMethod.POST, mapping = "/listAndArgs")
    public String listAndArgsAction(List<String> stringList, ActionRequest request, ActionResponse resp) {
        String result = list(stringList);
        
        if (request == null || resp == null)
            throw new RuntimeException("Missing parameters");
        
        return result;
    }
    
    @WebAction(httpMethods = HttpMethod.POST, mapping = "/dtoAndArgs")
    public String dtoAndArgsAction(JsonRequest request, Dto dto, ActionResponse resp) {
        String result = postDto(dto);
        
        if (request == null || resp == null)
            throw new RuntimeException("Missing parameters");
        
        return result;
    }
    
}
