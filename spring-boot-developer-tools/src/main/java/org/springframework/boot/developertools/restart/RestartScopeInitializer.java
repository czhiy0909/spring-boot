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

package org.springframework.boot.developertools.restart;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Support for a 'restart' {@link Scope} that allows beans to remain between restarts.
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public class RestartScopeInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Log logger = LogFactory.getLog(RestartScopeInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		applicationContext.getBeanFactory().registerScope("restart", new RestartScope());
	}

	private static class RestartScope implements Scope {

		@Override
		public Object get(String name, ObjectFactory<?> objectFactory) {
			Map<String, Object> attributes = Restarter.getInstance().getAttributes();
			synchronized (attributes) {
				if (attributes.containsKey(name)) {
					return attributes.get(name);
				}
				Object attribute = objectFactory.getObject();
				attributes.put(name, attribute);
				return attribute;
			}
		}

		@Override
		public Object remove(String name) {
			return Restarter.getInstance().getAttributes().remove(name);
		}

		@Override
		public void registerDestructionCallback(String name, Runnable callback) {
			logger.warn("Unable to add destruction callbacks to bean " + name
					+ " in restart scope");
		}

		@Override
		public Object resolveContextualObject(String key) {
			return null;
		}

		@Override
		public String getConversationId() {
			return null;
		}
	}

}
