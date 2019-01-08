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

package org.cloudfoundry.metrics.boot2;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import org.cloudfoundry.metrics.CloudFoundryMetricWriterProperties;
import org.cloudfoundry.metrics.Metric;
import org.cloudfoundry.metrics.MetricPublisher;
import org.cloudfoundry.metrics.Type;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

final class MicrometerMetricWriter extends StepMeterRegistry {

    private static final DecimalFormat PERCENTILE_FORMAT = new DecimalFormat("#.####");

    private final MetricPublisher metricPublisher;

    MicrometerMetricWriter(Clock clock, MetricPublisher metricPublisher, CloudFoundryMetricWriterProperties properties) {
        super(new MicrometerMetricWriterConfig(properties), clock);
        this.metricPublisher = metricPublisher;

        start();
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    @Override
    protected void publish() {
        List<Metric> metrics = getMeters().stream()
            .peek(m -> System.out.println(m.getId()))
            .flatMap(meter -> {
                if (meter instanceof DistributionSummary) {
                    return getMetrics((DistributionSummary) meter);
                } else if (meter instanceof FunctionTimer) {
                    return getMetrics((FunctionTimer) meter);
                } else if (meter instanceof Timer) {
                    return getMetrics((Timer) meter);
                } else {
                    return getMetrics(meter);
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        this.metricPublisher.publish(metrics);
    }

    private Stream<Metric> getMetrics(DistributionSummary meter) {
        return getMetrics(meter, meter.takeSnapshot());
    }

    private Stream<Metric> getMetrics(FunctionTimer meter) {
        return Stream.of(
            toMetric(withStatistic(meter, "count"), meter.count()),
            toMetric(withStatistic(meter, "mean"), meter.mean(getBaseTimeUnit())),
            toMetric(withStatistic(meter, "totalTime"), meter.totalTime(getBaseTimeUnit()))
        );
    }

    private Stream<Metric> getMetrics(Timer meter) {
        return getMetrics(meter, meter.takeSnapshot());
    }

    private Stream<Metric> getMetrics(Meter meter, HistogramSnapshot snapshot) {
        return Stream.concat(
            Stream.of(
                toMetric(withStatistic(meter, "count"), snapshot.count()),
                toMetric(withStatistic(meter, "max"), snapshot.max(getBaseTimeUnit())),
                toMetric(withStatistic(meter, "mean"), snapshot.mean(getBaseTimeUnit())),
                toMetric(withStatistic(meter, "totalTime"), snapshot.total(getBaseTimeUnit()))
            ),
            getMetrics(meter, snapshot.percentileValues())
        );
    }

    private Stream<Metric> getMetrics(Meter meter, ValueAtPercentile[] percentiles) {
        return Arrays.stream(percentiles)
            .map(percentile -> toMetric(withPercentile(meter, percentile), percentile.value(getBaseTimeUnit())));
    }

    private Stream<Metric> getMetrics(Meter meter) {
        return stream(meter.measure().spliterator(), false)
            .map(measurement -> toMetric(meter.getId().withTag(measurement.getStatistic()), measurement.getValue()));
    }

    private Metric toMetric(Meter.Id id, double value) {
        if (Double.isNaN(value)) {
            return null;
        }

        Map<String, String> tags = id.getTags().stream()
            .collect(Collectors.toMap(Tag::getKey, Tag::getValue));

        return new Metric(id.getName(), tags, this.clock.wallTime(), Type.GAUGE, id.getBaseUnit(), value);
    }

    private Meter.Id withPercentile(Meter meter, ValueAtPercentile percentile) {
        return withStatistic(meter, String.format("%spercentile", PERCENTILE_FORMAT.format(percentile.percentile() * 100)));
    }

    private Meter.Id withStatistic(Meter meter, String type) {
        return meter.getId().withTag(Tag.of("statistic", type));
    }

    private static final class MicrometerMetricWriterConfig implements StepRegistryConfig {

        private final CloudFoundryMetricWriterProperties properties;

        private MicrometerMetricWriterConfig(CloudFoundryMetricWriterProperties properties) {
            this.properties = properties;
        }

        @Override
        public String get(String k) {
            return null;
        }

        @Override
        public String prefix() {
            return "";
        }

        @Override
        public Duration step() {
            return Duration.ofMillis(properties.getRate());
        }

    }

}
