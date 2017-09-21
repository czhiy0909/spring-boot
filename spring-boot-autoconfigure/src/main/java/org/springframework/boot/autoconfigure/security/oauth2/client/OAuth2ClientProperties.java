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

package org.springframework.boot.autoconfigure.security.oauth2.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Madhura Bhave
 */
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuth2ClientProperties {

	private Map<String, Provider> provider = new HashMap<>();

	private Map<String, Registration> registration = new HashMap<>();

	public Map<String, Provider> getProvider() {
		return this.provider;
	}

	public Map<String, Registration> getRegistration() {
		return this.registration;
	}

	public static class Registration {

		private String provider;

		private String clientId;

		private String clientSecret;

		private ClientAuthenticationMethod clientAuthenticationMethod;

		private AuthorizationGrantType authorizationGrantType;

		private String redirectUri;

		private Set<String> scope;

		private String clientName;

		private String clientAlias;

		public String getProvider() {
			return this.provider;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public String getClientId() {
			return this.clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getClientSecret() {
			return this.clientSecret;
		}

		public void setClientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
		}

		public ClientAuthenticationMethod getClientAuthenticationMethod() {
			return this.clientAuthenticationMethod;
		}

		public void setClientAuthenticationMethod(
				ClientAuthenticationMethod clientAuthenticationMethod) {
			this.clientAuthenticationMethod = clientAuthenticationMethod;
		}

		public AuthorizationGrantType getAuthorizationGrantType() {
			return this.authorizationGrantType;
		}

		public void setAuthorizationGrantType(
				AuthorizationGrantType authorizationGrantType) {
			this.authorizationGrantType = authorizationGrantType;
		}

		public String getRedirectUri() {
			return this.redirectUri;
		}

		public void setRedirectUri(String redirectUri) {
			this.redirectUri = redirectUri;
		}

		public Set<String> getScope() {
			return this.scope;
		}

		public void setScope(Set<String> scope) {
			this.scope = scope;
		}

		public String getClientName() {
			return this.clientName;
		}

		public void setClientName(String clientName) {
			this.clientName = clientName;
		}

		public String getClientAlias() {
			return this.clientAlias;
		}

		public void setClientAlias(String clientAlias) {
			this.clientAlias = clientAlias;
		}
	}

	public static class Provider {

		private String authorizationUri;

		private String tokenUri;

		private String userInfoUri;

		private String jwkSetUri;

		private ClientAuthenticationMethod authenticationMethod;

		public String getAuthorizationUri() {
			return this.authorizationUri;
		}

		public void setAuthorizationUri(String authorizationUri) {
			this.authorizationUri = authorizationUri;
		}

		public String getTokenUri() {
			return this.tokenUri;
		}

		public void setTokenUri(String tokenUri) {
			this.tokenUri = tokenUri;
		}

		public String getUserInfoUri() {
			return this.userInfoUri;
		}

		public void setUserInfoUri(String userInfoUri) {
			this.userInfoUri = userInfoUri;
		}

		public String getJwkSetUri() {
			return this.jwkSetUri;
		}

		public void setJwkSetUri(String jwkSetUri) {
			this.jwkSetUri = jwkSetUri;
		}

		public ClientAuthenticationMethod getAuthenticationMethod() {
			return this.authenticationMethod;
		}

		public void setAuthenticationMethod(
				ClientAuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
		}

	}

}
