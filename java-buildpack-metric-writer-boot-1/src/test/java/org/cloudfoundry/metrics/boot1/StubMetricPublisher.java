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

package org.cloudfoundry.metrics.boot1;

import org.cloudfoundry.metrics.Metric;
import org.cloudfoundry.metrics.MetricPublisher;

import java.util.List;

final class StubMetricPublisher implements MetricPublisher {

    private List<Metric> metrics;

    @Override
    public void publish(List<Metric> metrics) {
        this.metrics = metrics;
    }

    List<Metric> getMetrics() {
        return this.metrics;
    }

}
