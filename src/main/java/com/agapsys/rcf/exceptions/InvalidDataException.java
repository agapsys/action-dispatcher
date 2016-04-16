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

public class InvalidDataException extends BadRequestException {

	public InvalidDataException() {
		this(null);
	}

	public InvalidDataException(Integer appStatus) {
		this(appStatus, "");
	}
	
	public InvalidDataException(String msg, Object...msgArgs) {
		this(null, msg, msgArgs);
	}
	
	public InvalidDataException(Integer appStatus, String msg, Object... msgArgs) {
		super(appStatus, msg, msgArgs);
	}
}
