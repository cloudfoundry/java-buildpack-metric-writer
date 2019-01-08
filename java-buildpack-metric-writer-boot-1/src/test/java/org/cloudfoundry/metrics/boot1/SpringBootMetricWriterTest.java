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
import org.cloudfoundry.metrics.Type;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.PublicMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class SpringBootMetricWriterTest {

    private final StubMetricPublisher metricPublisher = new StubMetricPublisher();

    private final List<PublicMetrics> metricsCollections = new ArrayList<>();

    private final CloudFoundryMetricWriterProperties properties = new CloudFoundryMetricWriterProperties(null, null, null, null, null, 60_000, false);

    private final SpringBootMetricWriter metricWriter = new SpringBootMetricWriter(this.metricsCollections, this.metricPublisher, this.properties);

    @Test
    public void publish() {
        this.metricsCollections.add(new StubPublicMetrics(getDopplerMetric(0), getDopplerMetric(1)));
        this.metricsCollections.add(new StubPublicMetrics(getDopplerMetric(2), getDopplerMetric(3)));

        this.metricWriter.publish();

        assertThat(this.metricPublisher.getMetrics()).contains(getMetric(0), getMetric(1), getMetric(2), getMetric(3));
    }

    private static org.springframework.boot.actuate.metrics.Metric<?> getDopplerMetric(int index) {
        return new org.springframework.boot.actuate.metrics.Metric<>(getName(index), index, new Date(index));
    }

    private static Metric getMetric(int index) {
        return new Metric(getName(index), Collections.emptyMap(), new Date(index).getTime(), Type.GAUGE, null, index);
    }

    private static String getName(int index) {
        return String.format("test-metric-%d", index);
    }

}
