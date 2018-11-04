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

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;

import jp.xet.spring.aws.configuration.AwsClientConfiguration.AwsClientProperties;
import jp.xet.spring.aws.configuration.AwsClientConfiguration.AwsS3ClientProperties;

/**
 * Spring configuration class to configure AWS client builders.
 *
 * @param <T> type of AWS client
 * @author miyamoto.daisuke
 */
@Slf4j
@RequiredArgsConstructor
class AwsClientFactoryBean<T>extends AbstractFactoryBean<T> {
	
	private static final String DEFAULT_NAME = "default";
	
	private static final String S3_BUILDER = "com.amazonaws.services.s3.AmazonS3Builder";
	
	private static final String ENCRYPTION_CLIENT_BUILDER = "com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder";
	
	static final String ENCRYPTION_MATERIALS_PROVIDER =
			"com.amazonaws.services.s3.model.EncryptionMaterialsProvider";
	
	
	private static Optional<AwsClientProperties> getAwsClientProperties(
			Map<String, AwsClientProperties> stringAwsClientPropertiesMap, Class<?> clientClass) {
		try {
			String servicePackageName = clientClass.getPackage().getName()
				.substring("com.amazonaws.services.".length())
				.replace('.', '-');
			
			if (clientClass.getName().endsWith("Async")) {
				AwsClientProperties asyncProperties = stringAwsClientPropertiesMap.get(servicePackageName + "-async");
				if (asyncProperties != null) {
					return Optional.of(asyncProperties);
				}
			}
			AwsClientProperties serviceProperties = stringAwsClientPropertiesMap.get(servicePackageName);
			if (serviceProperties != null) {
				return Optional.of(serviceProperties);
			}
			return Optional.ofNullable(stringAwsClientPropertiesMap.get(DEFAULT_NAME));
		} catch (IndexOutOfBoundsException e) {
			log.error("Failed to get property name: {}", clientClass);
			throw e;
		}
	}
	
	
	private final Class<?> builderClass;
	
	private final Class<T> clientClass;
	
	private final Map<String, AwsClientProperties> awsClientPropertiesMap;
	
	private final AwsS3ClientProperties awsS3ClientProperties;
	
	
	@Override
	public Class<?> getObjectType() {
		return clientClass;
	}
	
	@Override
	protected T createInstance() throws Exception {
		Object builder = AwsClientUtil.createBuilder(builderClass);
		
		if (builderClass.getName().startsWith("com.amazonaws.services.s3.")) {
			configureAmazonS3ClientBuilder(builder);
		}
		
		Optional<AwsClientProperties> specificConfig = getAwsClientProperties(awsClientPropertiesMap, clientClass);
		Optional<AwsClientProperties> defaultConfig = Optional.ofNullable(awsClientPropertiesMap.get(DEFAULT_NAME));
		
		ClientConfiguration clientConfiguration = specificConfig.map(AwsClientProperties::getClient)
			.orElseGet(() -> defaultConfig.map(AwsClientProperties::getClient).orElse(null));
		AwsClientUtil.configureClientConfiguration(builder, clientConfiguration);
		
		EndpointConfiguration endpointConfiguration = specificConfig.map(AwsClientProperties::getEndpoint)
			.orElseGet(() -> defaultConfig.map(AwsClientProperties::getEndpoint).orElse(null));
		if (endpointConfiguration != null) {
			AwsClientUtil.configureEndpointConfiguration(builder, endpointConfiguration);
		} else {
			String region = specificConfig.map(AwsClientProperties::getRegion)
				.orElseGet(() -> defaultConfig.map(AwsClientProperties::getRegion).orElse(null));
			if (region != null) {
				AwsClientUtil.configureRegion(builder, region);
			}
		}
		
		return AwsClientUtil.buildClient(builder);
	}
	
	private void configureAmazonS3ClientBuilder(Object builder) {
		try {
			if (Class.forName(S3_BUILDER).isAssignableFrom(builder.getClass())) {
				invokeMethod(builder, "setPathStyleAccessEnabled", awsS3ClientProperties.getPathStyleAccessEnabled());
				invokeMethod(builder, "setChunkedEncodingDisabled", awsS3ClientProperties.getChunkedEncodingDisabled());
				invokeMethod(builder, "setAccelerateModeEnabled", awsS3ClientProperties.getAccelerateModeEnabled());
				invokeMethod(builder, "setPayloadSigningEnabled", awsS3ClientProperties.getPayloadSigningEnabled());
				invokeMethod(builder, "setDualstackEnabled", awsS3ClientProperties.getDualstackEnabled());
				invokeMethod(builder, "setForceGlobalBucketAccessEnabled",
						awsS3ClientProperties.getForceGlobalBucketAccessEnabled());
			}
		} catch (ClassNotFoundException e) {
			log.debug(S3_BUILDER + " is not found in classpath -- ignored", e);
		}
		
		if (builderClass.getName().equals(ENCRYPTION_CLIENT_BUILDER)) {
			BeanFactory beanFactory = getBeanFactory();
			if (beanFactory != null && beanFactory.containsBean(ENCRYPTION_MATERIALS_PROVIDER)) {
				try {
					Object encryptionMaterial = beanFactory.getBean(ENCRYPTION_MATERIALS_PROVIDER);
					invokeMethod(builder, "setEncryptionMaterials", encryptionMaterial);
				} catch (IllegalStateException e) {
					log.warn(ENCRYPTION_MATERIALS_PROVIDER + " is not found in classpath -- ignored", e);
				}
			}
		}
	}
}