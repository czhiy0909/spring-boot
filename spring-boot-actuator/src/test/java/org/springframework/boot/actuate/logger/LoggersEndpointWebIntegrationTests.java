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

package org.springframework.boot.actuate.logger;

import org.junit.Ignore;
import org.junit.runner.RunWith;

import org.springframework.boot.actuate.endpoint.web.WebEndpointsRunner;
import org.springframework.boot.actuate.logging.LoggersEndpoint;

/**
 * Integration tests for {@link LoggersEndpoint} when exposed via Jersey, Spring MVC, and
 * WebFlux.
 *
 * @author Ben Hale
 * @author Phillip Webb
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 */
@Ignore
@RunWith(WebEndpointsRunner.class)
public class LoggersEndpointWebIntegrationTests {

	// FIXME

	// private static ConfigurableApplicationContext context;
	//
	// private static WebTestClient client;
	//
	// private LoggingSystem loggingSystem;
	//
	// @Before
	// @After
	// public void resetMocks() {
	// this.loggingSystem = context.getBean(LoggingSystem.class);
	// Mockito.reset(this.loggingSystem);
	// given(this.loggingSystem.getSupportedLogLevels())
	// .willReturn(EnumSet.allOf(LogLevel.class));
	// }
	//
	// @Test
	// public void getLoggerShouldReturnAllLoggerConfigurations() throws Exception {
	// given(this.loggingSystem.getLoggerConfigurations()).willReturn(Collections
	// .singletonList(new LoggerConfiguration("ROOT", null, LogLevel.DEBUG)));
	// client.get().uri("/application/loggers").exchange().expectStatus().isOk()
	// .expectBody().jsonPath("$.length()").isEqualTo(2).jsonPath("levels")
	// .isEqualTo(jsonArrayOf("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG",
	// "TRACE"))
	// .jsonPath("loggers.length()").isEqualTo(1)
	// .jsonPath("loggers.ROOT.length()").isEqualTo(2)
	// .jsonPath("loggers.ROOT.configuredLevel").isEqualTo(null)
	// .jsonPath("loggers.ROOT.effectiveLevel").isEqualTo("DEBUG");
	// }
	//
	// @Test
	// public void getLoggerShouldReturnLogLevels() throws Exception {
	// given(this.loggingSystem.getLoggerConfiguration("ROOT"))
	// .willReturn(new LoggerConfiguration("ROOT", null, LogLevel.DEBUG));
	// client.get().uri("/application/loggers/ROOT").exchange().expectStatus().isOk()
	// .expectBody().jsonPath("$.length()").isEqualTo(2)
	// .jsonPath("configuredLevel").isEqualTo(null).jsonPath("effectiveLevel")
	// .isEqualTo("DEBUG");
	// }
	//
	// @Test
	// public void getLoggersWhenLoggerNotFoundShouldReturnNotFound() throws Exception {
	// client.get().uri("/application/loggers/com.does.not.exist").exchange()
	// .expectStatus().isNotFound();
	// }
	//
	// @Test
	// public void setLoggerUsingApplicationJsonShouldSetLogLevel() throws Exception {
	// client.post().uri("/application/loggers/ROOT")
	// .contentType(MediaType.APPLICATION_JSON)
	// .syncBody(Collections.singletonMap("configuredLevel", "debug")).exchange()
	// .expectStatus().isNoContent();
	// verify(this.loggingSystem).setLogLevel("ROOT", LogLevel.DEBUG);
	// }
	//
	// @Test
	// public void setLoggerUsingActuatorV2JsonShouldSetLogLevel() throws Exception {
	// client.post().uri("/application/loggers/ROOT")
	// .contentType(ActuatorMediaTypes.APPLICATION_ACTUATOR_V2_JSON)
	// .syncBody(Collections.singletonMap("configuredLevel", "debug")).exchange()
	// .expectStatus().isNoContent();
	// verify(this.loggingSystem).setLogLevel("ROOT", LogLevel.DEBUG);
	// }
	//
	// @Test
	// public void setLoggerWithWrongLogLevelResultInBadRequestResponse() throws Exception
	// {
	// client.post().uri("/application/loggers/ROOT")
	// .contentType(MediaType.APPLICATION_JSON)
	// .syncBody(Collections.singletonMap("configuredLevel", "other")).exchange()
	// .expectStatus().isBadRequest();
	// verifyZeroInteractions(this.loggingSystem);
	// }
	//
	// @Test
	// public void setLoggerWithNullLogLevel() throws Exception {
	// client.post().uri("/application/loggers/ROOT")
	// .contentType(ActuatorMediaTypes.APPLICATION_ACTUATOR_V2_JSON)
	// .syncBody(Collections.singletonMap("configuredLevel", null)).exchange()
	// .expectStatus().isNoContent();
	// verify(this.loggingSystem).setLogLevel("ROOT", null);
	// }
	//
	// @Test
	// public void setLoggerWithNoLogLevel() throws Exception {
	// client.post().uri("/application/loggers/ROOT")
	// .contentType(ActuatorMediaTypes.APPLICATION_ACTUATOR_V2_JSON)
	// .syncBody(Collections.emptyMap()).exchange().expectStatus().isNoContent();
	// verify(this.loggingSystem).setLogLevel("ROOT", null);
	// }
	//
	// @Test
	// public void logLevelForLoggerWithNameThatCouldBeMistakenForAPathExtension()
	// throws Exception {
	// given(this.loggingSystem.getLoggerConfiguration("com.png"))
	// .willReturn(new LoggerConfiguration("com.png", null, LogLevel.DEBUG));
	// client.get().uri("/application/loggers/com.png").exchange().expectStatus().isOk()
	// .expectBody().jsonPath("$.length()").isEqualTo(2)
	// .jsonPath("configuredLevel").isEqualTo(null).jsonPath("effectiveLevel")
	// .isEqualTo("DEBUG");
	// }
	//
	// private JSONArray jsonArrayOf(Object... entries) {
	// JSONArray array = new JSONArray();
	// array.addAll(Arrays.asList(entries));
	// return array;
	// }
	//
	// @Configuration
	// static class TestConfiguration {
	//
	// @Bean
	// public LoggingSystem loggingSystem() {
	// return mock(LoggingSystem.class);
	// }
	//
	// @Bean
	// public LoggersEndpoint endpoint(LoggingSystem loggingSystem) {
	// return new LoggersEndpoint(loggingSystem);
	// }
	//
	// }

}
