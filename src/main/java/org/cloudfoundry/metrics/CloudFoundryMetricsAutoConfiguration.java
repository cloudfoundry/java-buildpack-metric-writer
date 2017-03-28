/*
 * Copyright 2016-2017 the original author or authors.
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

package org.cloudfoundry.metrics;

import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@ConditionalOnClass({RestTemplate.class, PublicMetrics.class})
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
@ConditionalOnProperty(prefix = "cloudfoundry.metrics", name = {"accessToken", "applicationId", "instanceId", "instanceIndex", "endpoint"})
@Configuration
@EnableConfigurationProperties(CloudFoundryMetricsProperties.class)
class CloudFoundryMetricsAutoConfiguration {

    @Bean
    CloudFoundryMetricsExporter cloudFoundryMetricWriter(CloudFoundryMetricsProperties properties,
                                                         Collection<PublicMetrics> publicMetrics) {

        return new CloudFoundryMetricsExporter(publicMetrics, properties, restTemplate(properties), scheduledExecutorService());
    }

    private RestTemplate restTemplate(CloudFoundryMetricsProperties properties) {
        if (properties.isSkipSslValidation()) {
            return new RestTemplate(new SkipSslVerificationHttpRequestFactory());
        }

        return new RestTemplate();
    }

    private ScheduledExecutorService scheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, String.format("cloudfoundry-metrics-exporter-%d", this.counter.getAndIncrement()));
                thread.setDaemon(true);
                return thread;
            }

        });
    }

}
