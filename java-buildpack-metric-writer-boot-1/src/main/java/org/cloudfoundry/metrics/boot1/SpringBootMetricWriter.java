/*
 * Copyright 2016-2019 the original author or authors.
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

package org.cloudfoundry.metrics.boot1;

import org.cloudfoundry.metrics.CloudFoundryMetricWriterProperties;
import org.cloudfoundry.metrics.Metric;
import org.cloudfoundry.metrics.MetricPublisher;
import org.cloudfoundry.metrics.Type;
import org.springframework.boot.actuate.endpoint.PublicMetrics;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

final class SpringBootMetricWriter {

    private final MetricPublisher metricPublisher;

    private final Collection<PublicMetrics> metricsCollections;

    private final CloudFoundryMetricWriterProperties properties;

    private ScheduledFuture<?> publisher;

    SpringBootMetricWriter(Collection<PublicMetrics> metricsCollections, MetricPublisher metricPublisher, CloudFoundryMetricWriterProperties properties) {
        this.metricsCollections = metricsCollections;
        this.metricPublisher = metricPublisher;
        this.properties = properties;
    }

    @PostConstruct
    public void start() {
        if (this.publisher != null) {
            stop();
        }

        this.publisher = Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(this::publish, properties.getRate(), properties.getRate(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        if (this.publisher != null) {
            this.publisher.cancel(true);
            this.publisher = null;
        }
    }

    void publish() {
        List<Metric> metrics = this.metricsCollections.stream()
            .flatMap(metricsCollection -> metricsCollection.metrics().stream())
            .map(metric -> new Metric(metric.getName(), Collections.emptyMap(), metric.getTimestamp().getTime(), Type.GAUGE, null, metric.getValue()))
            .collect(Collectors.toList());

        this.metricPublisher.publish(metrics);
    }

}
