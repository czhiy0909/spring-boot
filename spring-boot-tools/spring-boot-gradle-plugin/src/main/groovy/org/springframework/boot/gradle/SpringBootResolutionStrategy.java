
package org.springframework.boot.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.springframework.boot.dependency.tools.Dependency;
import org.springframework.boot.dependency.tools.ManagedDependencies;
import org.springframework.boot.dependency.tools.PropertiesFileManagedDependencies;
import org.springframework.boot.dependency.tools.VersionManagedDependencies;

/**
 * A resolution strategy to resolve missing version numbers using the
 * 'spring-boot-dependencies' POM.
 *
 * @author Phillip Webb
 */
public class SpringBootResolutionStrategy {

	public static final String VERSION_MANAGEMENT_CONFIGURATION = "versionManagement";

	private static final String SPRING_BOOT_GROUP = "org.springframework.boot";

	public static void apply(final Project project, Configuration configuration) {
		if (VERSION_MANAGEMENT_CONFIGURATION.equals(configuration.getName())) {
			return;
		}

		ResolutionStrategy resolutionStrategy = configuration.getResolutionStrategy();
		final Configuration versionManagementConfiguration = project.getConfigurations().getByName(
				VERSION_MANAGEMENT_CONFIGURATION);

		resolutionStrategy.eachDependency(new Action<DependencyResolveDetails>() {

			@Override
			public void execute(DependencyResolveDetails resolveDetails) {
				String version = resolveDetails.getTarget().getVersion();
				if (version == null || version.trim().length() == 0) {
					resolve(resolveDetails);
				}
			}

			private void resolve(DependencyResolveDetails resolveDetails) {

				ManagedDependencies dependencies = new VersionManagedDependencies(
						getVersionManagedDependencies());
				ModuleVersionSelector target = resolveDetails.getTarget();

				if (SPRING_BOOT_GROUP.equals(target.getGroup())) {
					resolveDetails.useVersion(dependencies.getSpringBootVersion());
					return;
				}

				Dependency dependency = dependencies.find(target.getGroup(),
						target.getName());
				if (dependency != null) {
					resolveDetails.useVersion(dependency.getVersion());
				}

			}

			private Collection<ManagedDependencies> getVersionManagedDependencies() {
				Set<File> files = versionManagementConfiguration.resolve();
				List<ManagedDependencies> dependencies = new ArrayList<ManagedDependencies>(
						files.size());
				for (File file : files) {
					if (!file.getName().toLowerCase().endsWith(".properties")) {
						throw new IllegalStateException(file
								+ " is not a version property file");
					}
					try {
						dependencies.add(new PropertiesFileManagedDependencies(
								new FileInputStream(file)));
					} catch (IOException ex) {
						throw new IllegalStateException(ex);
					}
				}
				return dependencies;
			}

		});
	}
}
