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

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public final class MicrometerMetricWriterTest {

    private final MockClock clock = new MockClock();

    private final StubMetricPublisher metricPublisher = new StubMetricPublisher();

    private final CloudFoundryMetricWriterProperties properties = new CloudFoundryMetricWriterProperties(null, null, null, null, null, 60_000, false);

    private final MicrometerMetricWriter metricWriter = new MicrometerMetricWriter(this.clock, this.metricPublisher, this.properties);

    @Test
    public void publish() {
        addMetric(0, this.metricWriter);
        addMetric(1, this.metricWriter);
        addMetric(2, this.metricWriter);
        addMetric(3, this.metricWriter);

        this.metricWriter.publish();

        assertThat(this.metricPublisher.getMetrics()).contains(getMetric(0), getMetric(1), getMetric(2), getMetric(3));
    }

    private static void addMetric(int index, MeterRegistry meterRegistry) {
        meterRegistry.gauge(getName(index), Collections.singleton(new ImmutableTag(getTagKey(index), getTagValue(index))), index);
    }

    private static Metric getMetric(int index) {
        Map<String, String> tags = new HashMap<>(2);
        tags.put("statistic", "value");
        tags.put(getTagKey(index), getTagValue(index));

        return new Metric(getName(index), tags, 0L, Type.GAUGE, null, (double) index);
    }

    private static String getName(int index) {
        return String.format("test-metric-%d", index);
    }

    private static String getTagKey(int index) {
        return String.format("test-metric-%d-key", index);
    }

    private static String getTagValue(int index) {
        return String.format("test-metric-%d-value", index);
    }

}
