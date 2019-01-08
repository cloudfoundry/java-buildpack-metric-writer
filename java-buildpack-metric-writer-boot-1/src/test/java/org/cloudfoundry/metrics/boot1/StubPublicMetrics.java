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

import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

final class StubPublicMetrics implements PublicMetrics {

    private final List<Metric<?>> metrics;

    StubPublicMetrics(Metric<?>... metrics) {
        this.metrics = Arrays.asList(metrics);
    }

    @Override
    public Collection<Metric<?>> metrics() {
        return this.metrics;
    }

}
