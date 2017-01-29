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
package com.agapsys.rcf.exceptions;

public class MethodNotAllowedException extends ClientException {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    public static final int CODE = 405;
    // =========================================================================
    // </editor-fold>

    public MethodNotAllowedException() {
        this(null);
    }

    public MethodNotAllowedException(Integer appStatus) {
        this(appStatus, "");
    }

    public MethodNotAllowedException(String msg, Object...msgArgs) {
        this(null, msg, msgArgs);
    }

    public MethodNotAllowedException(Integer appStatus, String msg, Object... msgArgs) {
        super(CODE, appStatus, msg, msgArgs);
    }

}
