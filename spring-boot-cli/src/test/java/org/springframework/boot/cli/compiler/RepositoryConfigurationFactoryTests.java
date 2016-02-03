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

package org.springframework.boot.cli.compiler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;
import org.springframework.boot.cli.util.SystemProperties;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RepositoryConfigurationFactory}
 *
 * @author Andy Wilkinson
 */
public class RepositoryConfigurationFactoryTests {

	@Test
	public void defaultRepositories() {
		SystemProperties.doWithSystemProperties(new Runnable() {
			@Override
			public void run() {
				List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
						.createDefaultRepositoryConfiguration();
				assertRepositoryConfiguration(repositoryConfiguration, "central", "local",
						"spring-snapshot", "spring-milestone");
			}
		}, "user.home:src/test/resources/maven-settings/basic");
	}

	@Test
	public void snapshotRepositoriesDisabled() {
		SystemProperties.doWithSystemProperties(new Runnable() {
			@Override
			public void run() {
				List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
						.createDefaultRepositoryConfiguration();
				assertRepositoryConfiguration(repositoryConfiguration, "central",
						"local");
			}
		}, "user.home:src/test/resources/maven-settings/basic",
				"disableSpringSnapshotRepos:true");
	}

	@Test
	public void activeByDefaultProfileRepositories() {
		SystemProperties.doWithSystemProperties(new Runnable() {
			@Override
			public void run() {
				List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
						.createDefaultRepositoryConfiguration();
				assertRepositoryConfiguration(repositoryConfiguration, "central", "local",
						"spring-snapshot", "spring-milestone", "active-by-default");
			}
		}, "user.home:src/test/resources/maven-settings/active-profile-repositories");
	}

	@Test
	public void activeByPropertyProfileRepositories() {
		SystemProperties.doWithSystemProperties(new Runnable() {
			@Override
			public void run() {
				List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
						.createDefaultRepositoryConfiguration();
				assertRepositoryConfiguration(repositoryConfiguration, "central", "local",
						"spring-snapshot", "spring-milestone", "active-by-property");
			}
		}, "user.home:src/test/resources/maven-settings/active-profile-repositories",
				"foo:bar");
	}

	@Test
	public void interpolationProfileRepositories() {
		SystemProperties.doWithSystemProperties(new Runnable() {
			@Override
			public void run() {
				List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
						.createDefaultRepositoryConfiguration();
				assertRepositoryConfiguration(repositoryConfiguration, "central", "local",
						"spring-snapshot", "spring-milestone", "interpolate-releases",
						"interpolate-snapshots");
			}
		}, "user.home:src/test/resources/maven-settings/active-profile-repositories",
				"interpolate:true");
	}

	private void assertRepositoryConfiguration(
			List<RepositoryConfiguration> configurations, String... expectedNames) {
		assertThat(configurations, hasSize(expectedNames.length));
		Set<String> actualNames = new HashSet<String>();
		for (RepositoryConfiguration configuration : configurations) {
			actualNames.add(configuration.getName());
		}
		assertThat(actualNames, hasItems(expectedNames));
	}
}
