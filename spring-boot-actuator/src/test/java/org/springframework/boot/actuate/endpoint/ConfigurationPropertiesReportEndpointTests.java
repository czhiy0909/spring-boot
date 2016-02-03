/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.boot.actuate.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationPropertiesReportEndpoint}.
 *
 * @author Dave Syer
 */
public class ConfigurationPropertiesReportEndpointTests
		extends AbstractEndpointTests<ConfigurationPropertiesReportEndpoint> {

	public ConfigurationPropertiesReportEndpointTests() {
		super(Config.class, ConfigurationPropertiesReportEndpoint.class, "configprops",
				true, "endpoints.configprops");
	}

	@Test
	public void testInvoke() throws Exception {
		assertThat(getEndpointBean().invoke().size(), greaterThan(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNaming() throws Exception {
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) properties
				.get("testProperties");
		assertNotNull(nestedProperties);
		assertEquals("test", nestedProperties.get("prefix"));
		assertNotNull(nestedProperties.get("properties"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDefaultKeySanitization() throws Exception {
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertNotNull(nestedProperties);
		assertEquals("******", nestedProperties.get("dbPassword"));
		assertEquals("654321", nestedProperties.get("myTestProperty"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testKeySanitization() throws Exception {
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		report.setKeysToSanitize("property");
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertNotNull(nestedProperties);
		assertEquals("123456", nestedProperties.get("dbPassword"));
		assertEquals("******", nestedProperties.get("myTestProperty"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testKeySanitizationWithCustomPattern() throws Exception {
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		report.setKeysToSanitize(".*pass.*");
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertNotNull(nestedProperties);
		assertEquals("******", nestedProperties.get("dbPassword"));
		assertEquals("654321", nestedProperties.get("myTestProperty"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testKeySanitizationWithCustomKeysByEnvironment() throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"endpoints.configprops.keys-to-sanitize:property");
		this.context.register(Config.class);
		this.context.refresh();
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertNotNull(nestedProperties);
		assertEquals("123456", nestedProperties.get("dbPassword"));
		assertEquals("******", nestedProperties.get("myTestProperty"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testKeySanitizationWithCustomPatternByEnvironment() throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"endpoints.configprops.keys-to-sanitize: .*pass.*");
		this.context.register(Config.class);
		this.context.refresh();
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertNotNull(nestedProperties);
		assertEquals("******", nestedProperties.get("dbPassword"));
		assertEquals("654321", nestedProperties.get("myTestProperty"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testKeySanitizationWithCustomPatternAndKeyByEnvironment()
			throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"endpoints.configprops.keys-to-sanitize: .*pass.*, property");
		this.context.register(Config.class);
		this.context.refresh();
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertNotNull(nestedProperties);
		assertEquals("******", nestedProperties.get("dbPassword"));
		assertEquals("******", nestedProperties.get("myTestProperty"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testKeySanitizationWithCustomPatternUsingCompositeKeys()
			throws Exception {
		// gh-4415
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"endpoints.configprops.keys-to-sanitize: .*\\.secrets\\..*, .*\\.hidden\\..*");
		this.context.register(Config.class);
		this.context.refresh();
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertNotNull(nestedProperties);
		Map<String, Object> secrets = (Map<String, Object>) nestedProperties
				.get("secrets");
		Map<String, Object> hidden = (Map<String, Object>) nestedProperties.get("hidden");
		assertEquals("******", secrets.get("mine"));
		assertEquals("******", secrets.get("yours"));
		assertEquals("******", hidden.get("mine"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void mixedBoolean() throws Exception {
		ConfigurationPropertiesReportEndpoint report = getEndpointBean();
		Map<String, Object> properties = report.invoke();
		Map<String, Object> nestedProperties = (Map<String, Object>) ((Map<String, Object>) properties
				.get("testProperties")).get("properties");
		assertThat(nestedProperties.get("mixedBoolean"), equalTo((Object) true));
	}

	@Configuration
	@EnableConfigurationProperties
	public static class Parent {
		@Bean
		public TestProperties testProperties() {
			return new TestProperties();
		}
	}

	@Configuration
	@EnableConfigurationProperties
	public static class Config {

		@Bean
		public ConfigurationPropertiesReportEndpoint endpoint() {
			return new ConfigurationPropertiesReportEndpoint();
		}

		@Bean
		public TestProperties testProperties() {
			return new TestProperties();
		}

	}

	@ConfigurationProperties(prefix = "test")
	public static class TestProperties {

		private String dbPassword = "123456";

		private String myTestProperty = "654321";

		private Boolean mixedBoolean = true;

		private Map<String, Object> secrets = new HashMap<String, Object>();

		private Hidden hidden = new Hidden();

		public TestProperties() {
			this.secrets.put("mine", "myPrivateThing");
			this.secrets.put("yours", "yourPrivateThing");
		}

		public String getDbPassword() {
			return this.dbPassword;
		}

		public void setDbPassword(String dbPassword) {
			this.dbPassword = dbPassword;
		}

		public String getMyTestProperty() {
			return this.myTestProperty;
		}

		public void setMyTestProperty(String myTestProperty) {
			this.myTestProperty = myTestProperty;
		}

		public boolean isMixedBoolean() {
			return (this.mixedBoolean == null ? false : this.mixedBoolean);
		}

		public void setMixedBoolean(Boolean mixedBoolean) {
			this.mixedBoolean = mixedBoolean;
		}

		public Map<String, Object> getSecrets() {
			return this.secrets;
		}

		public void setSecrets(Map<String, Object> secrets) {
			this.secrets = secrets;
		}

		public Hidden getHidden() {
			return this.hidden;
		}

		public void setHidden(Hidden hidden) {
			this.hidden = hidden;
		}

		public static class Hidden {

			private String mine = "mySecret";

			public String getMine() {
				return this.mine;
			}

			public void setMine(String mine) {
				this.mine = mine;
			}

		}

	}
}
