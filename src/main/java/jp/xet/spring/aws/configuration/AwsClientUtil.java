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
package jp.xet.spring.aws.configuration;

import static jp.xet.spring.aws.configuration.InternalReflectionUtil.invokeMethod;
import static jp.xet.spring.aws.configuration.InternalReflectionUtil.invokeStaticMethod;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;

@Slf4j
class AwsClientUtil {
	
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
	static <T> T buildClient(Object builder) {
		return invokeMethod(builder, "build");
	}
}