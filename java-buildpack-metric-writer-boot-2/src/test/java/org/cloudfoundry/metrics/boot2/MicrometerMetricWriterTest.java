/*
 * Copyright 2016-2018 the original author or authors.
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

package org.cloudfoundry.metrics.boot2;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.cloudfoundry.metrics.CloudFoundryMetricWriterProperties;
import org.cloudfoundry.metrics.Metric;
import org.cloudfoundry.metrics.Type;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public final class MicrometerMetricWriterTest {

    private final MockClock clock = new MockClock();

    private final StubMetricPublisher metricPublisher = new StubMetricPublisher();

    private final MicrometerMetricWriter metricWriter;

    private final CloudFoundryMetricWriterProperties properties = new CloudFoundryMetricWriterProperties(null, null, null, null, null, 60_000, false);

    public MicrometerMetricWriterTest() {
        this.metricWriter = new MicrometerMetricWriter(this.clock, this.metricPublisher, this.properties);
        this.metricWriter.config().meterFilter(new MeterFilter() {

            @Override
            public DistributionStatisticConfig configure(Meter.Id mappedId, DistributionStatisticConfig config) {
                return DistributionStatisticConfig.builder()
                    .percentiles(0.50)
                    .build()
                    .merge(config);
            }

        });
    }

    @Test
    public void publishMeter() {
        addDistributionSummary(0, this.metricWriter);
        addFunctionTimer(1, this.metricWriter);
        addTimer(2, this.metricWriter);
        addMeter(3, this.metricWriter);

        this.metricWriter.publish();

        assertThat(this.metricPublisher.getMetrics()).contains(
            getMetric(0, "count"),
            getMetric(0, "max"),
            getMetric(0, "mean"),
            getMetric(0, "totalTime"),
            getMetric(0, "50percentile"),
            getMetric(1, "count", "milliseconds"),
            getMetric(1, "mean", "milliseconds"),
            getMetric(1, "totalTime", "milliseconds"),
            getMetric(2, "count", "milliseconds"),
            getMetric(2, "max", "milliseconds"),
            getMetric(2, "mean", "milliseconds"),
            getMetric(2, "totalTime", "milliseconds"),
            getMetric(2, "50percentile", "milliseconds"),
            getMetric(3, "value")
        );
    }

    private static void addDistributionSummary(int index, MeterRegistry meterRegistry) {
        meterRegistry.summary(getName(index));
    }

    private static void addFunctionTimer(int index, MeterRegistry meterRegistry) {
        meterRegistry.more().timer(getName(index), Collections.emptySet(), null, null, null, null);
    }

    private static void addMeter(int index, MeterRegistry meterRegistry) {
        meterRegistry.gauge(getName(index), 0);
    }

    private static void addTimer(int index, MeterRegistry meterRegistry) {
        meterRegistry.timer(getName(index));
    }

    private static Metric getMetric(int index, String statistic) {
        return getMetric(index, statistic, null);
    }

    private static Metric getMetric(int index, String statistic, String unit) {
        return new Metric(getName(index), Collections.singletonMap("statistic", statistic), 0L, Type.GAUGE, unit, 0.0);
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
