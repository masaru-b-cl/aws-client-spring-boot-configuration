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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Enables AWS client capability.   To be used
 * on @{@link org.springframework.context.annotation.Configuration} classes as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableEnableAwsClient({AmazonS3.class, AmazonEC2Async.class})
 * public class AppConfig {
 *
 *     // various &#064;Bean definitions
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
	AwsBeanDefinitionRegistrar.class,
	AwsClientConfiguration.class
})
public @interface EnableAwsClient {
	
	/**
	 * AWS client classes to be used in the application.
	 * 
	 * @return array of AWS client classes
	 */
	Class<?>[] value() default {};
}