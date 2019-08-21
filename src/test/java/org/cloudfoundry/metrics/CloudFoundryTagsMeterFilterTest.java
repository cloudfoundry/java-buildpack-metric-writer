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

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class CloudFoundryTagsMeterFilterTest {

    @Test
    void defer() {
        Tags originalTags = Tags.of(
            Tag.of("cf.account", "test-account-original"),
            Tag.of("cf.application", "test-application-original"),
            Tag.of("cf.cluster", "test-cluster-original"),
            Tag.of("cf.instance.index", "test-instance-index-original"),
            Tag.of("cf.organization", "test-organization-original"),
            Tag.of("cf.space", "test-space-original"),
            Tag.of("cf.version", "test-version-original")
        );

        Meter.Id id = new CloudFoundryTagsMeterFilter("test-account", "test-application", "test-cluster", "test-instance-index", "test-organization", "test-space", "test-version")
            .map(new Meter.Id("test", originalTags, null, null, Meter.Type.GAUGE));

        assertThat(id.getTags()).containsExactly(
            Tag.of("cf.account", "test-account-original"),
            Tag.of("cf.application", "test-application-original"),
            Tag.of("cf.cluster", "test-cluster-original"),
            Tag.of("cf.instance.index", "test-instance-index-original"),
            Tag.of("cf.organization", "test-organization-original"),
            Tag.of("cf.space", "test-space-original"),
            Tag.of("cf.version", "test-version-original")
        );
    }

    @Test
    void nulls() {
        Meter.Id id = new CloudFoundryTagsMeterFilter(null, null, null, null, null, null, null)
            .map(new Meter.Id("test", Tags.empty(), null, null, Meter.Type.GAUGE));

        assertThat(id.getTags().isEmpty()).isTrue();
    }

    @Test
    void values() {
        Meter.Id id = new CloudFoundryTagsMeterFilter("test-account", "test-application", "test-cluster", "test-instance-index", "test-organization", "test-space", "test-version")
            .map(new Meter.Id("test", Tags.empty(), null, null, Meter.Type.GAUGE));

        assertThat(id.getTags()).containsExactly(
            Tag.of("cf.account", "test-account"),
            Tag.of("cf.application", "test-application"),
            Tag.of("cf.cluster", "test-cluster"),
            Tag.of("cf.instance.index", "test-instance-index"),
            Tag.of("cf.organization", "test-organization"),
            Tag.of("cf.space", "test-space"),
            Tag.of("cf.version", "test-version")
        );
    }
}