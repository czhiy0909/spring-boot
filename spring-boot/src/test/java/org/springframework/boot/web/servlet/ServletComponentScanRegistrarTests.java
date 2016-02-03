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

package org.springframework.boot.web.servlet;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.Matchers.containsInAnyOrder;


/**
 * Tests for {@link ServletComponentScanRegistrar}
 *
 * @author Andy Wilkinson
 */
public class ServletComponentScanRegistrarTests {

	private AnnotationConfigApplicationContext context;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@After
	public void after() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void packagesConfiguredWithValue() {
		this.context = new AnnotationConfigApplicationContext(ValuePackages.class);
		ServletComponentRegisteringPostProcessor postProcessor = this.context
				.getBean(ServletComponentRegisteringPostProcessor.class);
		assertThat(postProcessor.getPackagesToScan(),
				containsInAnyOrder("com.example.foo", "com.example.bar"));
	}

	@Test
	public void packagesConfiguredWithBackPackages() {
		this.context = new AnnotationConfigApplicationContext(BasePackages.class);
		ServletComponentRegisteringPostProcessor postProcessor = this.context
				.getBean(ServletComponentRegisteringPostProcessor.class);
		assertThat(postProcessor.getPackagesToScan(),
				containsInAnyOrder("com.example.foo", "com.example.bar"));
	}

	@Test
	public void packagesConfiguredWithBasePackageClasses() {
		this.context = new AnnotationConfigApplicationContext(BasePackageClasses.class);
		ServletComponentRegisteringPostProcessor postProcessor = this.context
				.getBean(ServletComponentRegisteringPostProcessor.class);
		assertThat(postProcessor.getPackagesToScan(),
				containsInAnyOrder(getClass().getPackage().getName()));
	}

	@Test
	public void packagesConfiguredWithBothValueAndBasePackages() {
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("@ServletComponentScan basePackages and value"
				+ " attributes are mutually exclusive");
		this.context = new AnnotationConfigApplicationContext(ValueAndBasePackages.class);
		ServletComponentRegisteringPostProcessor postProcessor = this.context
				.getBean(ServletComponentRegisteringPostProcessor.class);
		assertThat(postProcessor.getPackagesToScan(),
				containsInAnyOrder(getClass().getPackage().getName()));
	}

	@Test
	public void packagesFromMultipleAnnotationsAreMerged() {
		this.context = new AnnotationConfigApplicationContext(BasePackages.class,
				AdditionalPackages.class);
		ServletComponentRegisteringPostProcessor postProcessor = this.context
				.getBean(ServletComponentRegisteringPostProcessor.class);
		assertThat(postProcessor.getPackagesToScan(), containsInAnyOrder(
				"com.example.foo", "com.example.bar", "com.example.baz"));
	}

	@Configuration
	@ServletComponentScan({ "com.example.foo", "com.example.bar" })
	static class ValuePackages {

	}

	@Configuration
	@ServletComponentScan(basePackages = { "com.example.foo", "com.example.bar" })
	static class BasePackages {

	}

	@Configuration
	@ServletComponentScan(basePackages = "com.example.baz")
	static class AdditionalPackages {

	}

	@Configuration
	@ServletComponentScan(basePackageClasses = ServletComponentScanRegistrarTests.class)
	static class BasePackageClasses {

	}

	@Configuration
	@ServletComponentScan(value = "com.example.foo", basePackages = "com.example.bar")
	static class ValueAndBasePackages {

	}

}
