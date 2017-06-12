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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class CloudFoundryMetricsExporter implements Runnable {

    private static final String RETRY_AFTER = "X-RateLimit-Retry-After";

    private final Log logger = LogFactory.getLog(CloudFoundryMetricsExporter.class);

    private final MetricCache cache = new MetricCache();

    private final Collection<PublicMetrics> metricsCollections;

    private final CloudFoundryMetricsProperties properties;

    private final RestOperations restOperations;

    private final ScheduledExecutorService scheduledExecutorService;

    private ScheduledFuture<?> execution;

    private volatile Long retryAfter;

    CloudFoundryMetricsExporter(Collection<PublicMetrics> metricsCollections, CloudFoundryMetricsProperties properties, RestOperations restOperations,
                                ScheduledExecutorService scheduledExecutorService) {

        this.properties = properties;
        this.metricsCollections = metricsCollections;
        this.restOperations = restOperations;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        if (this.retryAfter != null && this.retryAfter > System.currentTimeMillis()) {
            this.logger.debug("Skipping sending Spring Boot metrics to Metrics Forwarder service");
            return;
        }

        this.logger.debug("Sending Spring Boot metrics to Metrics Forwarder service");

        List<Metric> metrics = getMetrics();

        try {
            this.restOperations.postForEntity(this.properties.getEndpoint(), getRequest(metrics), Void.class);
            this.logger.debug("Sent Spring Boot metrics to Metrics Forwarder service");
            this.retryAfter = null;
        } catch (Exception e) {
            if (e instanceof HttpStatusCodeException) {
                HttpStatus statusCode = ((HttpStatusCodeException) e).getStatusCode();

                if (HttpStatus.UNPROCESSABLE_ENTITY == statusCode) {
                    this.logger.error("Failed to send Spring Boot metrics to Metrics Forwarder service due to unprocessable payload.  Discarding metrics.", e);
                } else if (HttpStatus.PAYLOAD_TOO_LARGE == statusCode) {
                    this.logger.error("Failed to send Spring Boot metrics to Metrics Forwarder service due to rate limiting.  Discarding metrics.", e);
                } else if (HttpStatus.TOO_MANY_REQUESTS == statusCode) {
                    this.logger.error("Failed to send Spring Boot metrics to Metrics Forwarder service due to rate limiting.  Caching metrics.", e);
                    this.cache.addAll(metrics);
                } else {
                    this.logger.error("Failed to send Spring Boot metrics to Metrics Forwarder service. Caching metrics.", e);
                    this.cache.addAll(metrics);
                }
            }

            this.retryAfter = getRetryAfter(e);
        }
    }

    @PostConstruct
    public void start() {
        this.execution = this.scheduledExecutorService.scheduleAtFixedRate(this, this.properties.getRate(), this.properties.getRate(), TimeUnit.MILLISECONDS);
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
        this.logger.info("Exporting Spring Boot metrics to Metrics Forwarder service");
    }

    private List<Metric> getMetrics() {
        List<Metric> metrics = this.cache.getAndClear();

        for (PublicMetrics metricsCollection : this.metricsCollections) {
            for (org.springframework.boot.actuate.metrics.Metric<?> metric : metricsCollection.metrics()) {
                metrics.add(new Metric(metric));
            }
        }

        return metrics;
    }

    private Payload getPayload(List<Metric> metrics) {
        Instance instance = new Instance(this.properties.getInstanceId(), this.properties.getInstanceIndex(), metrics);
        Application application = new Application(this.properties.getApplicationId(), Collections.singletonList(instance));
        return new Payload(Collections.singletonList(application));
    }

    private HttpEntity<Payload> getRequest(List<Metric> metrics) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, this.properties.getAccessToken());

        return new HttpEntity<>(getPayload(metrics), headers);
    }

    private Long getRetryAfter(Exception candidate) {
        if (candidate instanceof RestClientResponseException) {
            String retryAfter = ((RestClientResponseException) candidate).getResponseHeaders().getFirst(RETRY_AFTER);

            if (retryAfter != null) {
                return System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Long.parseLong(retryAfter));
            }
        }

        return null;
    }

}
