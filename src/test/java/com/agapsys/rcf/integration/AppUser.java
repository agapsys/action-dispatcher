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
package com.agapsys.rcf.integration;

import com.agapsys.rcf.User;
import java.util.LinkedHashSet;
import java.util.Set;

// STATIC CLASS ============================================================

public class AppUser implements User {

    public final Set<String> roles = new LinkedHashSet<>();
    public final long perms;

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public long getPermissions() {
        return perms;
    }

    public AppUser(long perms) {
        this(perms, new String[] {});
    }

    public AppUser(String...roles) {
        this(0, roles);
    }

    public AppUser(long perms, String... roles) {
        for (String role : roles) {
            if (role != null) {
                this.roles.add(role);
            }
        }

        this.perms = perms;
    }

}
