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

package org.springframework.boot.autoconfigure.transaction.jta;import static org.assertj.core.api.Assertions.assertThat;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * External configuration properties for a {@link JtaTransactionManager} created by
 * Spring. All {@literal spring.jta.} properties are also applied to the appropriate
 * vendor specific configuration.
 *
 * @author Josh Long
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.2.0
 */
@ConfigurationProperties(prefix = JtaProperties.PREFIX, ignoreUnknownFields = true)
public class JtaProperties {

	public static final String PREFIX = "spring.jta";

	/**
	 * Transaction logs directory.
	 */
	private String logDir;

	/**
	 * Transaction manager unique identifier.
	 */
	private String transactionManagerId;

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	public String getLogDir() {
		return this.logDir;
	}

	public String getTransactionManagerId() {
		return this.transactionManagerId;
	}

	public void setTransactionManagerId(String transactionManagerId) {
		this.transactionManagerId = transactionManagerId;
	}

}
