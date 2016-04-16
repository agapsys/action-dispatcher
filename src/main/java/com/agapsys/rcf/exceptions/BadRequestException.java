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

package com.agapsys.rcf.exceptions;

/** Exception thrown when a request does does not contain expected data. */
public class BadRequestException extends ClientException {
	// CLASS SCOPE =============================================================
	public static final int CODE = 400;
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	public BadRequestException() {
		this(null);
	}

	public BadRequestException(Integer appStatus) {
		this(appStatus, "");
	}

	public BadRequestException(String msg, Object...msgArgs) {
		this(null, msg, msgArgs);
	}
	
	public BadRequestException(Integer appStatus, String msg, Object... msgArgs) {
		super(CODE, appStatus, msg, msgArgs);
	}
	// =========================================================================
}
