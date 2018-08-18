/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.xet.spring.aws.autoconfigure;

import static jp.xet.spring.aws.autoconfigure.InternalReflectionUtil.invokeMethod;
import static jp.xet.spring.aws.autoconfigure.InternalReflectionUtil.invokeStaticMethod;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.ReflectionUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;

@Slf4j
class AwsClientUtil {
	
	/**
	 * Determine AWS client class from AWS client builder class.
	 * 
	 * @param builderClass AWS client builder class
	 * @return AWS client class
	 * @throws ClientClassNotDeterminedException if client class cannot be determined from {@code builderClass}
	 */
	static Class<?> getClientClass(Class<?> builderClass) {
		try {
			ParameterizedType t = (ParameterizedType) builderClass.getGenericSuperclass();
			return (Class<?>) t.getActualTypeArguments()[1];
		} catch (ClassCastException e) {
			log.warn("Failed to get client type from generics: {}", builderClass);
			
			Method build = ReflectionUtils.findMethod(builderClass, "build");
			if (build != null) {
				Class<?> returnType = build.getReturnType();
				
				if (returnType.getPackage().getName().startsWith("com.amazonaws.services")) {
					return returnType;
				}
			}
			log.error("Client class cannot be determined: {}", builderClass);
			throw new ClientClassNotDeterminedException(e);
		}
	}
	
	/**
	 * Create AWS client builder.
	 * 
	 * @param builderClass AWS client builder class
	 * @return AWS client builder
	 */
	static Object createBuilder(Class<?> builderClass) {
		return invokeStaticMethod(builderClass, "standard");
	}
	
	static void configureClientConfiguration(Object builder, ClientConfiguration clientConfiguration) {
		if (clientConfiguration == null) {
			return;
		}
		try {
			invokeMethod(builder, "setClientConfiguration", clientConfiguration);
		} catch (IllegalStateException e) {
			log.warn(e.getMessage());
		}
	}
	
	static void configureEndpointConfiguration(Object builder, EndpointConfiguration endpointConfiguration) {
		if (endpointConfiguration == null) {
			return;
		}
		try {
			invokeMethod(builder, "setEndpointConfiguration", endpointConfiguration);
		} catch (IllegalStateException e) {
			log.warn(e.getMessage());
		}
	}
	
	static void configureRegion(Object builder, String region) {
		if (region == null) {
			return;
		}
		try {
			invokeMethod(builder, "setRegion", region);
		} catch (IllegalStateException e) {
			log.warn(e.getMessage());
		}
	}
	
	/**
	 * Build AWS client.
	 * 
	 * @param builder AWS client builder
	 * @return AWS client
	 */
	static Object buildClient(Object builder) {
		return invokeMethod(builder, "build");
	}
}
