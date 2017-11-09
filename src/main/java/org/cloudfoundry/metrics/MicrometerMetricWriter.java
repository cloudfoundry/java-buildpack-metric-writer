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

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

final class MicrometerMetricWriter extends StepMeterRegistry {

    private final MetricPublisher metricPublisher;

    MicrometerMetricWriter(Clock clock, MetricPublisher metricPublisher, CloudFoundryMetricWriterProperties properties) {
        super(new MicrometerMetricWriterConfig(properties), clock);
        this.metricPublisher = metricPublisher;
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    @Override
    protected void publish() {
        List<Metric> metrics = getMeters().stream()
            .flatMap(meter -> stream(meter.measure().spliterator(), false)
                .map(measurement -> toMetric(meter, measurement)))
            .collect(Collectors.toList());

        this.metricPublisher.publish(metrics);
    }

    private Metric toMetric(Meter meter, Measurement measurement) {
        Meter.Id id = meter.getId().withTag(measurement.getStatistic());

        Map<String, String> tags = stream(id.getTags().spliterator(), false)
            .collect(Collectors.toMap(Tag::getKey, Tag::getValue));

        return new Metric(id.getName(), tags, this.clock.wallTime(), Type.GAUGE, id.getBaseUnit(), measurement.getValue());
    }

    private static final class MicrometerMetricWriterConfig implements StepRegistryConfig {

        private final CloudFoundryMetricWriterProperties properties;

        private MicrometerMetricWriterConfig(CloudFoundryMetricWriterProperties properties) {
            this.properties = properties;
        }

        @Override
        public String get(String k) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String prefix() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Duration step() {
            return Duration.ofMillis(properties.getRate());
        }

    }

}
