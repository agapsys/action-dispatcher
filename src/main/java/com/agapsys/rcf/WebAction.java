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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Web action annotation.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebAction {
	/** @return Accepted HTTP methods */
	HttpMethod[] httpMethods()    default {HttpMethod.GET};
	
	/** @return name of the mapping. Passing an null/empty string will use the method name as path mapping. */
	String       mapping()       default "";
	
	/** @return a boolean indicating if annotated method will be the default action handled by servlet. */
	boolean      defaultAction() default false;
}
