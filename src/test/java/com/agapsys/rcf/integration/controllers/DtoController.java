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
package com.agapsys.rcf.integration.controllers;

import com.agapsys.rcf.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;

@WebController("dto")
public class DtoController extends Controller {
    
    public static class SourceObject implements Dto {
        public final int srcVal;

        public SourceObject(int val) {
            this.srcVal = val;
        }

        @Override
        public String toString() {
            return String.format("srcVal:%d", srcVal);
        }

        @Override
        public Object getDto() {
            return new DoubleDto(this);
        }
    }

    public static class DoubleDto {
        public final int dtoVal;

        public DoubleDto(SourceObject obj) {
            this.dtoVal = obj.srcVal * 2;
        }

        @Override
        public String toString() {
            return String.format("dtoVal:%d", dtoVal);
        }
    }

    @WebAction
    public SourceObject getObject(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        return new SourceObject(1);
    }

    @WebAction
    public List<SourceObject> getList(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        List<SourceObject> list = new LinkedList<>();
        list.add(new SourceObject(0));
        list.add(new SourceObject(1));
        list.add(new SourceObject(2));

        return list;
    }

    @WebAction
    public Set<SourceObject> getSet(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        Set<SourceObject> set = new LinkedHashSet<>();
        set.add(new SourceObject(3));
        set.add(new SourceObject(4));
        set.add(new SourceObject(5));
        return set;
    }

    @WebAction
    public Map<Object, SourceObject> getMap(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        Map<Object, SourceObject> map = new LinkedHashMap<>();
        map.put("a", new SourceObject(1));
        map.put("b", new SourceObject(3));
        map.put("c", new SourceObject(5));
        return map;
    }

}
