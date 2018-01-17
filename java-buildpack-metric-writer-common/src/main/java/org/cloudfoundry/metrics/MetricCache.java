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

package org.cloudfoundry.metrics;

import java.util.ArrayList;
import java.util.List;

final class MetricCache {

    private final List<Metric> cache = new ArrayList<>();

    private final Object monitor = new Object();

    boolean addAll(List<Metric> items) {
        synchronized (this.monitor) {
            return this.cache.addAll(items);
        }
    }

    List<Metric> getAndClear() {
        synchronized (this.monitor) {
            List<Metric> items = new ArrayList<>(this.cache);
            this.cache.clear();
            return items;
        }
    }

}
