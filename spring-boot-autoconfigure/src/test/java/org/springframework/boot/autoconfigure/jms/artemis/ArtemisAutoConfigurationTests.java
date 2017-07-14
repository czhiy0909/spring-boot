/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.autoconfigure.jms.artemis;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.TopicConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.TopicConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.StandardApplicationContextTester;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArtemisAutoConfiguration}.
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 */
public class ArtemisAutoConfigurationTests {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final StandardApplicationContextTester contextLoader = new StandardApplicationContextTester()
			.register(AutoConfigurations.of(ArtemisAutoConfiguration.class,
					JmsAutoConfiguration.class));

	@Test
	public void nativeConnectionFactory() {
		this.contextLoader.config(EmptyConfiguration.class)
				.env("spring.artemis.mode:native").load(context -> {
					JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
					ActiveMQConnectionFactory connectionFactory = context
							.getBean(ActiveMQConnectionFactory.class);
					assertThat(connectionFactory)
							.isEqualTo(jmsTemplate.getConnectionFactory());
					assertNettyConnectionFactory(connectionFactory, "localhost", 61616);
					assertThat(connectionFactory.getUser()).isNull();
					assertThat(connectionFactory.getPassword()).isNull();
				});
	}

	@Test
	public void nativeConnectionFactoryCustomHost() {
		this.contextLoader
				.config(EmptyConfiguration.class).env("spring.artemis.mode:native",
						"spring.artemis.host:192.168.1.144", "spring.artemis.port:9876")
				.load(context -> {
					ActiveMQConnectionFactory connectionFactory = context
							.getBean(ActiveMQConnectionFactory.class);
					assertNettyConnectionFactory(connectionFactory, "192.168.1.144",
							9876);
				});
	}

	@Test
	public void nativeConnectionFactoryCredentials() {
		this.contextLoader
				.config(EmptyConfiguration.class).env("spring.artemis.mode:native",
						"spring.artemis.user:user", "spring.artemis.password:secret")
				.load(context -> {
					JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
					ActiveMQConnectionFactory connectionFactory = context
							.getBean(ActiveMQConnectionFactory.class);
					assertThat(connectionFactory)
							.isEqualTo(jmsTemplate.getConnectionFactory());
					assertNettyConnectionFactory(connectionFactory, "localhost", 61616);
					assertThat(connectionFactory.getUser()).isEqualTo("user");
					assertThat(connectionFactory.getPassword()).isEqualTo("secret");
				});
	}

	@Test
	public void embeddedConnectionFactory() {
		this.contextLoader.config(EmptyConfiguration.class)
				.env("spring.artemis.mode:embedded").load(context -> {
					ArtemisProperties properties = context
							.getBean(ArtemisProperties.class);
					assertThat(properties.getMode()).isEqualTo(ArtemisMode.EMBEDDED);
					assertThat(context.getBeansOfType(EmbeddedJMS.class)).hasSize(1);
					org.apache.activemq.artemis.core.config.Configuration configuration = context
							.getBean(
									org.apache.activemq.artemis.core.config.Configuration.class);
					assertThat(configuration.isPersistenceEnabled()).isFalse();
					assertThat(configuration.isSecurityEnabled()).isFalse();
					ActiveMQConnectionFactory connectionFactory = context
							.getBean(ActiveMQConnectionFactory.class);
					assertInVmConnectionFactory(connectionFactory);
				});
	}

	@Test
	public void embeddedConnectionFactoryByDefault() {
		// No mode is specified
		this.contextLoader.config(EmptyConfiguration.class).load(context -> {
			assertThat(context.getBeansOfType(EmbeddedJMS.class)).hasSize(1);
			org.apache.activemq.artemis.core.config.Configuration configuration = context
					.getBean(org.apache.activemq.artemis.core.config.Configuration.class);
			assertThat(configuration.isPersistenceEnabled()).isFalse();
			assertThat(configuration.isSecurityEnabled()).isFalse();

			ActiveMQConnectionFactory connectionFactory = context
					.getBean(ActiveMQConnectionFactory.class);
			assertInVmConnectionFactory(connectionFactory);
		});
	}

	@Test
	public void nativeConnectionFactoryIfEmbeddedServiceDisabledExplicitly() {
		// No mode is specified
		this.contextLoader.config(EmptyConfiguration.class)
				.env("spring.artemis.embedded.enabled:false").load(context -> {
					assertThat(context.getBeansOfType(EmbeddedJMS.class)).isEmpty();
					ActiveMQConnectionFactory connectionFactory = context
							.getBean(ActiveMQConnectionFactory.class);
					assertNettyConnectionFactory(connectionFactory, "localhost", 61616);
				});
	}

	@Test
	public void embeddedConnectionFactoryEvenIfEmbeddedServiceDisabled() {
		// No mode is specified
		this.contextLoader.config(EmptyConfiguration.class)
				.env("spring.artemis.mode:embedded",
						"spring.artemis.embedded.enabled:false")
				.load(context -> {
					assertThat(context.getBeansOfType(EmbeddedJMS.class)).isEmpty();
					ActiveMQConnectionFactory connectionFactory = context
							.getBean(ActiveMQConnectionFactory.class);
					assertInVmConnectionFactory(connectionFactory);
				});
	}

	@Test
	public void embeddedServerWithDestinations() {
		this.contextLoader.config(EmptyConfiguration.class)
				.env("spring.artemis.embedded.queues=Queue1,Queue2",
						"spring.artemis.embedded.topics=Topic1")
				.load(context -> {
					DestinationChecker checker = new DestinationChecker(context);
					checker.checkQueue("Queue1", true);
					checker.checkQueue("Queue2", true);
					checker.checkQueue("QueueWillNotBeAutoCreated", true);
					checker.checkTopic("Topic1", true);
					checker.checkTopic("TopicWillBeAutoCreated", true);
				});
	}

	@Test
	public void embeddedServerWithDestinationConfig() {
		this.contextLoader.config(DestinationConfiguration.class).load(context -> {
			DestinationChecker checker = new DestinationChecker(context);
			checker.checkQueue("sampleQueue", true);
			checker.checkTopic("sampleTopic", true);
		});
	}

	@Test
	public void embeddedServiceWithCustomJmsConfiguration() {
		// Ignored with custom config
		this.contextLoader.config(CustomJmsConfiguration.class)
				.env("spring.artemis.embedded.queues=Queue1,Queue2").load(context -> {
					DestinationChecker checker = new DestinationChecker(context);
					checker.checkQueue("custom", true); // See CustomJmsConfiguration
					checker.checkQueue("Queue1", true);
					checker.checkQueue("Queue2", true);
				});
	}

	@Test
	public void embeddedServiceWithCustomArtemisConfiguration() {
		this.contextLoader.config(CustomArtemisConfiguration.class).load(context -> {
			org.apache.activemq.artemis.core.config.Configuration configuration = context
					.getBean(org.apache.activemq.artemis.core.config.Configuration.class);
			assertThat(configuration.getName()).isEqualTo("customFooBar");
		});
	}

	@Test
	public void embeddedWithPersistentMode() throws IOException, JMSException {
		File dataFolder = this.folder.newFolder();
		final String msgId = UUID.randomUUID().toString();

		// Start the server and post a message to some queue
		this.contextLoader.config(EmptyConfiguration.class).env(
				"spring.artemis.embedded.queues=TestQueue",
				"spring.artemis.embedded.persistent:true",
				"spring.artemis.embedded.dataDirectory:" + dataFolder.getAbsolutePath())
				.load(context -> {

					JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
					jmsTemplate.send("TestQueue", new MessageCreator() {
						@Override
						public Message createMessage(Session session)
								throws JMSException {
							return session.createTextMessage(msgId);
						}
					});
				});

		// Start the server again and check if our message is still here
		this.contextLoader.load(context -> {

			JmsTemplate jmsTemplate2 = context.getBean(JmsTemplate.class);
			jmsTemplate2.setReceiveTimeout(1000L);
			Message message = jmsTemplate2.receive("TestQueue");
			assertThat(message).isNotNull();
			assertThat(((TextMessage) message).getText()).isEqualTo(msgId);
		});
	}

	@Test
	public void severalEmbeddedBrokers() {
		this.contextLoader.config(EmptyConfiguration.class)
				.env("spring.artemis.embedded.queues=Queue1").load(rootContext -> {
					this.contextLoader.env("spring.artemis.embedded.queues=Queue2")
							.load(anotherContext -> {
						ArtemisProperties properties = rootContext
								.getBean(ArtemisProperties.class);
						ArtemisProperties anotherProperties = anotherContext
								.getBean(ArtemisProperties.class);
						assertThat(
								properties.getEmbedded().getServerId() < anotherProperties
										.getEmbedded().getServerId()).isTrue();
						DestinationChecker checker = new DestinationChecker(
								anotherContext);
						checker.checkQueue("Queue1", true);
						checker.checkQueue("Queue2", true);
						DestinationChecker anotherChecker = new DestinationChecker(
								anotherContext);
						anotherChecker.checkQueue("Queue2", true);
						anotherChecker.checkQueue("Queue1", true);
					});
				});
	}

	@Test
	public void connectToASpecificEmbeddedBroker() {
		this.contextLoader.config(EmptyConfiguration.class)
				.env("spring.artemis.embedded.serverId=93",
						"spring.artemis.embedded.queues=Queue1")
				.load(context -> {
					this.contextLoader.config(EmptyConfiguration.class).env(
							"spring.artemis.mode=embedded",
							"spring.artemis.embedded.serverId=93", /*
																	 * Connect to the
																	 * "main" broker
																	 */
							"spring.artemis.embedded.enabled=false" /*
																	 * do not start a
																	 * specific one
																	 */)
							.load(anotherContext -> {
						DestinationChecker checker = new DestinationChecker(context);
						checker.checkQueue("Queue1", true);

						DestinationChecker anotherChecker = new DestinationChecker(
								anotherContext);
						anotherChecker.checkQueue("Queue1", true);
					});
				});
	}

	private TransportConfiguration assertInVmConnectionFactory(
			ActiveMQConnectionFactory connectionFactory) {
		TransportConfiguration transportConfig = getSingleTransportConfiguration(
				connectionFactory);
		assertThat(transportConfig.getFactoryClassName())
				.isEqualTo(InVMConnectorFactory.class.getName());
		return transportConfig;
	}

	private TransportConfiguration assertNettyConnectionFactory(
			ActiveMQConnectionFactory connectionFactory, String host, int port) {
		TransportConfiguration transportConfig = getSingleTransportConfiguration(
				connectionFactory);
		assertThat(transportConfig.getFactoryClassName())
				.isEqualTo(NettyConnectorFactory.class.getName());
		assertThat(transportConfig.getParams().get("host")).isEqualTo(host);
		assertThat(transportConfig.getParams().get("port")).isEqualTo(port);
		return transportConfig;
	}

	private TransportConfiguration getSingleTransportConfiguration(
			ActiveMQConnectionFactory connectionFactory) {
		TransportConfiguration[] transportConfigurations = connectionFactory
				.getServerLocator().getStaticTransportConfigurations();
		assertThat(transportConfigurations.length).isEqualTo(1);
		return transportConfigurations[0];
	}

	private final static class DestinationChecker {

		private final JmsTemplate jmsTemplate;

		private final DestinationResolver destinationResolver;

		private DestinationChecker(ApplicationContext applicationContext) {
			this.jmsTemplate = applicationContext.getBean(JmsTemplate.class);
			this.destinationResolver = new DynamicDestinationResolver();
		}

		public void checkQueue(String name, boolean shouldExist) {
			checkDestination(name, false, shouldExist);
		}

		public void checkTopic(String name, boolean shouldExist) {
			checkDestination(name, true, shouldExist);
		}

		public void checkDestination(final String name, final boolean pubSub,
				final boolean shouldExist) {
			this.jmsTemplate.execute(new SessionCallback<Void>() {
				@Override
				public Void doInJms(Session session) throws JMSException {
					try {
						Destination destination = DestinationChecker.this.destinationResolver
								.resolveDestinationName(session, name, pubSub);
						if (!shouldExist) {
							throw new IllegalStateException("Destination '" + name
									+ "' was not expected but got " + destination);
						}
					}
					catch (JMSException e) {
						if (shouldExist) {
							throw new IllegalStateException("Destination '" + name
									+ "' was expected but got " + e.getMessage());
						}
					}
					return null;
				}
			});
		}

	}

	@Configuration
	protected static class EmptyConfiguration {

	}

	@Configuration
	protected static class DestinationConfiguration {

		@Bean
		JMSQueueConfiguration sampleQueueConfiguration() {
			JMSQueueConfigurationImpl jmsQueueConfiguration = new JMSQueueConfigurationImpl();
			jmsQueueConfiguration.setName("sampleQueue");
			jmsQueueConfiguration.setSelector("foo=bar");
			jmsQueueConfiguration.setDurable(false);
			jmsQueueConfiguration.setBindings("/queue/1");
			return jmsQueueConfiguration;
		}

		@Bean
		TopicConfiguration sampleTopicConfiguration() {
			TopicConfigurationImpl topicConfiguration = new TopicConfigurationImpl();
			topicConfiguration.setName("sampleTopic");
			topicConfiguration.setBindings("/topic/1");
			return topicConfiguration;
		}

	}

	@Configuration
	protected static class CustomJmsConfiguration {

		@Bean
		public JMSConfiguration myJmsConfiguration() {
			JMSConfiguration config = new JMSConfigurationImpl();
			JMSQueueConfiguration jmsQueueConfiguration = new JMSQueueConfigurationImpl();
			jmsQueueConfiguration.setName("custom");
			jmsQueueConfiguration.setDurable(false);
			config.getQueueConfigurations().add(jmsQueueConfiguration);
			return config;
		}

	}

	@Configuration
	protected static class CustomArtemisConfiguration {

		@Bean
		public ArtemisConfigurationCustomizer myArtemisCustomize() {
			return new ArtemisConfigurationCustomizer() {
				@Override
				public void customize(
						org.apache.activemq.artemis.core.config.Configuration configuration) {
					configuration.setClusterPassword("Foobar");
					configuration.setName("customFooBar");
				}
			};
		}

	}

}
