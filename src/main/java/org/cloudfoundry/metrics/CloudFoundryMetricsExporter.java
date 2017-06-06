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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class CloudFoundryMetricsExporter implements Runnable {

    private final Log logger = LogFactory.getLog(CloudFoundryMetricsExporter.class);

    private final URI endpoint;

    private final Collection<PublicMetrics> metricsCollections;

    private final CloudFoundryMetricsProperties properties;

    private final RestOperations restOperations;

    private final ScheduledExecutorService scheduledExecutorService;

    private ScheduledFuture<?> execution;

    CloudFoundryMetricsExporter(Collection<PublicMetrics> metricsCollections, CloudFoundryMetricsProperties properties, RestOperations restOperations,
                                ScheduledExecutorService scheduledExecutorService) {

        this.endpoint = getEndpoint(properties.getEndpoint());
        this.properties = properties;
        this.metricsCollections = metricsCollections;
        this.restOperations = restOperations;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        this.logger.debug("Sending Spring Boot metrics to PCF Metrics");

        List<Metric> metrics = getMetrics(this.metricsCollections);
        HttpEntity<Payload> request = getRequest(getPayload(this.properties.getApplicationId(), this.properties.getInstanceId(), this.properties.getInstanceIndex(), metrics));

        try {
            this.restOperations.postForEntity(this.endpoint, request, Void.class);
            this.logger.debug("Sent Spring Boot metrics to PCF Metrics");
        } catch (Exception e) {
            this.logger.error("Failed to send Spring Boot metrics to PCF Metrics", e);
            // TODO: Handle too many requests
        }
    }

    @PostConstruct
    public void start() {
        this.execution = this.scheduledExecutorService.scheduleAtFixedRate(this, 0, this.properties.getRate(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        if (this.execution != null) {
            this.execution.cancel(true);
        }

        this.scheduledExecutorService.shutdownNow();
    }

    @PostConstruct
    void announce() {
        this.logger.info("Exporting Spring Boot metrics to PCF Metrics");
    }

    private static URI getEndpoint(String endpoint) {
        return UriComponentsBuilder.fromUriString(endpoint)
            .pathSegment("v1", "metrics")
            .build()
            .toUri();
    }

    private static List<Metric> getMetrics(Collection<PublicMetrics> metricsCollections) {
        List<Metric> metrics = new ArrayList<>();

        for (PublicMetrics metricsCollection : metricsCollections) {
            for (org.springframework.boot.actuate.metrics.Metric<?> metric : metricsCollection.metrics()) {
                metrics.add(new Metric(metric));
            }
        }

        return metrics;
    }

    private static Payload getPayload(String applicationId, String instanceId, String instanceIndex, List<Metric> metrics) {
        Instance instance = new Instance(instanceId, instanceIndex, metrics);
        Application application = new Application(applicationId, Collections.singletonList(instance));
        return new Payload(Collections.singletonList(application));
    }

    private HttpEntity<Payload> getRequest(Payload payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, this.properties.getAccessToken());

        return new HttpEntity<>(payload, headers);
    }

}
