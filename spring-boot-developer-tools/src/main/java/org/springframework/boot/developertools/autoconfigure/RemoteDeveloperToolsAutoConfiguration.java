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

package org.springframework.boot.developertools.autoconfigure;

import java.util.Collection;

import javax.servlet.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.developertools.remote.server.AccessManager;
import org.springframework.boot.developertools.remote.server.Dispatcher;
import org.springframework.boot.developertools.remote.server.DispatcherFilter;
import org.springframework.boot.developertools.remote.server.Handler;
import org.springframework.boot.developertools.remote.server.HandlerMapper;
import org.springframework.boot.developertools.remote.server.HttpHeaderAccessManager;
import org.springframework.boot.developertools.remote.server.UrlHandlerMapper;
import org.springframework.boot.developertools.restart.server.DefaultSourceFolderUrlFilter;
import org.springframework.boot.developertools.restart.server.HttpRestartServer;
import org.springframework.boot.developertools.restart.server.HttpRestartServerHandler;
import org.springframework.boot.developertools.restart.server.SourceFolderUrlFilter;
import org.springframework.boot.developertools.tunnel.server.HttpTunnelServer;
import org.springframework.boot.developertools.tunnel.server.HttpTunnelServerHandler;
import org.springframework.boot.developertools.tunnel.server.RemoteDebugPortProvider;
import org.springframework.boot.developertools.tunnel.server.SocketTargetServerConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for remote development support.
 *
 * @author Phillip Webb
 * @author Rob Winch
 * @since 1.3.0
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.developertools.remote", name = "secret")
@ConditionalOnClass(Filter.class)
@EnableConfigurationProperties(DeveloperToolsProperties.class)
public class RemoteDeveloperToolsAutoConfiguration {

	private static final Log logger = LogFactory
			.getLog(RemoteDeveloperToolsAutoConfiguration.class);

	@Autowired
	private DeveloperToolsProperties properties;

	@Bean
	@ConditionalOnMissingBean
	public AccessManager remoteDeveloperToolsAccessManager() {
		RemoteDeveloperToolsProperties remoteProperties = this.properties.getRemote();
		return new HttpHeaderAccessManager(remoteProperties.getSecretHeaderName(),
				remoteProperties.getSecret());
	}

	@Bean
	@ConditionalOnMissingBean
	public DispatcherFilter remoteDeveloperToolsDispatcherFilter(
			AccessManager accessManager, Collection<HandlerMapper> mappers) {
		Dispatcher dispatcher = new Dispatcher(accessManager, mappers);
		return new DispatcherFilter(dispatcher);
	}

	/**
	 * Configuration for remote update and restarts.
	 */
	@ConditionalOnProperty(prefix = "spring.developertools.remote.restart", name = "enabled", matchIfMissing = true)
	static class RemoteRestartConfiguration {

		@Autowired
		private DeveloperToolsProperties properties;

		@Bean
		@ConditionalOnMissingBean
		public SourceFolderUrlFilter remoteRestartSourceFolderUrlFilter() {
			return new DefaultSourceFolderUrlFilter();
		}

		@Bean
		@ConditionalOnMissingBean
		public HttpRestartServer httpRestartServer(
				SourceFolderUrlFilter sourceFolderUrlFilter) {
			return new HttpRestartServer(sourceFolderUrlFilter);
		}

		@Bean
		@ConditionalOnMissingBean(name = "remoteRestartHanderMapper")
		public UrlHandlerMapper remoteRestartHanderMapper(HttpRestartServer server) {
			String url = this.properties.getRemote().getContextPath() + "/restart";
			logger.warn("Listening for remote restart updates on " + url);
			Handler handler = new HttpRestartServerHandler(server);
			return new UrlHandlerMapper(url, handler);
		}

	}

	/**
	 * Configuration for remote debug HTTP tunneling.
	 */
	@ConditionalOnProperty(prefix = "spring.developertools.remote.debug", name = "enabled", matchIfMissing = true)
	static class RemoteDebugTunnelConfiguration {

		@Autowired
		private DeveloperToolsProperties properties;

		@Bean
		@ConditionalOnMissingBean(name = "remoteDebugHanderMapper")
		public UrlHandlerMapper remoteDebugHanderMapper(
				@Qualifier("remoteDebugHttpTunnelServer") HttpTunnelServer server) {
			String url = this.properties.getRemote().getContextPath() + "/debug";
			logger.warn("Listening for remote debug traffic on " + url);
			Handler handler = new HttpTunnelServerHandler(server);
			return new UrlHandlerMapper(url, handler);
		}

		@Bean
		@ConditionalOnMissingBean(name = "remoteDebugHttpTunnelServer")
		public HttpTunnelServer remoteDebugHttpTunnelServer() {
			return new HttpTunnelServer(new SocketTargetServerConnection(
					new RemoteDebugPortProvider()));
		}

	}

}
