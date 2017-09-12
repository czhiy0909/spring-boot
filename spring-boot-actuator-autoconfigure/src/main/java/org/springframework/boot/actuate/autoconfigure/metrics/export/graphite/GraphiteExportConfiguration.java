/**
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
package org.springframework.boot.actuate.autoconfigure.metrics.export.graphite;

import org.springframework.boot.actuate.autoconfigure.metrics.export.DurationConverter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.MetricsExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;

/**
 * @since 2.0.0
 * @author Jon Schneider
 */
@Configuration
@ConditionalOnClass(name = "io.micrometer.graphite.GraphiteMeterRegistry")
@Import(DurationConverter.class)
@EnableConfigurationProperties(GraphiteConfigurationProperties.class)
public class GraphiteExportConfiguration {
    @ConditionalOnProperty(value = "metrics.graphite.enabled", matchIfMissing = true)
    @Bean
    public MetricsExporter graphiteExporter(GraphiteConfig config, HierarchicalNameMapper nameMapper, Clock clock) {
        return () -> new GraphiteMeterRegistry(config, nameMapper, clock);
    }

    @ConditionalOnMissingBean
    @Bean
    public Clock clock() {
        return Clock.SYSTEM;
    }

    @ConditionalOnMissingBean
    @Bean
    public HierarchicalNameMapper hierarchicalNameMapper() {
        return HierarchicalNameMapper.DEFAULT;
    }
}
