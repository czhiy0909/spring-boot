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

package org.springframework.boot.autoconfigure.cassandra;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Cassandra.
 *
 * @author Julien Dubois
 * @author Phillip Webb
 * @since 1.3.0
 */
@Configuration
@ConditionalOnClass({ Cluster.class })
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraAutoConfiguration {

	@Autowired
	private CassandraProperties properties;

	@Bean
	@ConditionalOnMissingBean
	public Cluster cluster() {
		Cluster.Builder builder = Cluster.builder()
				.withClusterName(this.properties.getClusterName())
				.withPort(this.properties.getPort());
		if (this.properties.getCompression() != null) {
			builder.withCompression(this.properties.getCompression());
		}
		if (this.properties.getLoadBalancingPolicy() != null) {
			builder.withLoadBalancingPolicy(BeanUtils.instantiate(this.properties
					.getLoadBalancingPolicy()));
		}
		builder.withQueryOptions(getQueryOptions());
		if (this.properties.getReconnectionPolicy() != null) {
			builder.withReconnectionPolicy(BeanUtils.instantiate(this.properties
					.getReconnectionPolicy()));
		}
		if (this.properties.getRetryPolicy() != null) {
			builder.withRetryPolicy(BeanUtils.instantiate(this.properties
					.getRetryPolicy()));
		}
		builder.withSocketOptions(getSocketOptions());
		if (this.properties.isSsl()) {
			builder.withSSL();
		}
		builder.addContactPoints(StringUtils
				.commaDelimitedListToStringArray(this.properties.getContactPoints()));
		return builder.build();
	}

	private QueryOptions getQueryOptions() {
		QueryOptions options = new QueryOptions();
		if (this.properties.getConsistencyLevel() != null) {
			options.setConsistencyLevel(this.properties.getConsistencyLevel());
		}
		if (this.properties.getSerialConsistencyLevel() != null) {
			options.setSerialConsistencyLevel(this.properties.getSerialConsistencyLevel());
		}
		options.setFetchSize(this.properties.getFetchSize());
		return options;
	}

	private SocketOptions getSocketOptions() {
		SocketOptions options = new SocketOptions();
		options.setConnectTimeoutMillis(this.properties.getConnectTimeoutMillis());
		options.setReadTimeoutMillis(this.properties.getReadTimeoutMillis());
		return options;
	}

}
