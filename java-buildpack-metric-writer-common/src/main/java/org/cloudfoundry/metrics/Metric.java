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

package org.cloudfoundry.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

/**
 * A type encapsulating the metric payload expected by the Metric Forwarder
 */
public final class Metric {

    private final String name;

    private final Map<String, String> tags;

    private final Long timestamp;

    private final Type type;

    private final String unit;

    private final Number value;

    /**
     * Creates a new instance
     *
     * @param name      the name of the metric
     * @param tags      the tags associated with the metric
     * @param timestamp the timestamp of the metric
     * @param type      the type of the metric
     * @param unit      the unit of the metric value
     * @param value     the metric value
     */
    public Metric(String name, Map<String, String> tags, Long timestamp, Type type, String unit, Number value) {
        Assert.notNull(name, "name must not be null");
        Assert.notNull(tags, "tags must not be null");
        Assert.notNull(timestamp, "timestamp must not be null");
        Assert.notNull(type, "type must not be null");
        Assert.notNull(value, "value must not be null");

        this.name = name;
        this.tags = tags;
        this.timestamp = timestamp;
        this.type = type;
        this.unit = unit;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return Objects.equals(name, metric.name) &&
            Objects.equals(tags, metric.tags) &&
            Objects.equals(timestamp, metric.timestamp) &&
            type == metric.type &&
            Objects.equals(unit, metric.unit) &&
            Objects.equals(value, metric.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tags, timestamp, type, unit, value);
    }

    @Override
    public String toString() {
        return "Metric{" +
            "name='" + name + '\'' +
            ", tags=" + tags +
            ", timestamp=" + timestamp +
            ", type=" + type +
            ", unit='" + unit + '\'' +
            ", value=" + value +
            '}';
    }

    @JsonProperty("name")
    String getName() {
        return this.name;
    }

    @JsonProperty("tags")
    Map<String, String> getTags() {
        return this.tags;
    }

    @JsonProperty("timestamp")
    Long getTimestamp() {
        return this.timestamp;
    }

    @JsonProperty("type")
    Type getType() {
        return this.type;
    }

    @JsonProperty("unit")
    String getUnit() {
        return this.unit;
    }

    @JsonProperty("value")
    Number getValue() {
        return this.value;
    }

}
