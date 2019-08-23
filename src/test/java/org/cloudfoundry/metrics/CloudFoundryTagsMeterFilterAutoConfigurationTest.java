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
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

final class CloudFoundryTagsMeterFilterAutoConfigurationTest {

    @Test
    void configured() {
        Meter.Id id = new CloudFoundryTagsMeterFilterAutoConfiguration().meterFilter(new MockEnvironment()
            .withProperty("cf.instance.index", "test-instance-index")
            .withProperty("vcap.application.cf_api", "test-cf-api")
            .withProperty("vcap.application.application_name", "test.application-r042")
            .withProperty("vcap.application.organization_name", "test-organization-name")
            .withProperty("vcap.application.space_name", "test-space-name")
            .withProperty("cf.app.account", "configured-account")
            .withProperty("cf.app.application", "configured-application")
            .withProperty("cf.app.cluster", "configured-cluster")
            .withProperty("cf.app.instance.index", "configured-instance-index")
            .withProperty("cf.app.organization", "configured-organization-name")
            .withProperty("cf.app.space", "configured-space-name")
            .withProperty("cf.app.version", "configured-version")
        ).map(new Meter.Id("test", Tags.empty(), null, null, Meter.Type.GAUGE));

        assertThat(id.getTags()).containsExactly(
            Tag.of("cf.account", "configured-account"),
            Tag.of("cf.application", "configured-application"),
            Tag.of("cf.cluster", "configured-cluster"),
            Tag.of("cf.instance.index", "configured-instance-index"),
            Tag.of("cf.organization", "configured-organization-name"),
            Tag.of("cf.space", "configured-space-name"),
            Tag.of("cf.version", "configured-version")
        );
    }

    @Test
    void defaults() {
        Meter.Id id = new CloudFoundryTagsMeterFilterAutoConfiguration().meterFilter(new MockEnvironment()
            .withProperty("cf.instance.index", "test-instance-index")
            .withProperty("vcap.application.cf_api", "test-cf-api")
            .withProperty("vcap.application.application_name", "test.application-r042")
            .withProperty("vcap.application.organization_name", "test-organization-name")
            .withProperty("vcap.application.space_name", "test-space-name")
        ).map(new Meter.Id("test", Tags.empty(), null, null, Meter.Type.GAUGE));

        assertThat(id.getTags()).containsExactly(
            Tag.of("cf.account", "test-cf-api"),
            Tag.of("cf.application", "test.application"),
            Tag.of("cf.cluster", "test.application-r042"),
            Tag.of("cf.instance.index", "test-instance-index"),
            Tag.of("cf.organization", "test-organization-name"),
            Tag.of("cf.space", "test-space-name"),
            Tag.of("cf.version", "42")
        );
    }

    @Test
    void defaultsWithoutOrganizationName() {
        Meter.Id id = new CloudFoundryTagsMeterFilterAutoConfiguration().meterFilter(new MockEnvironment()
            .withProperty("cf.instance.index", "test-instance-index")
            .withProperty("vcap.application.cf_api", "test-cf-api")
            .withProperty("vcap.application.application_name", "test.application-r042")
            .withProperty("vcap.application.space_name", "test-space-name")
        ).map(new Meter.Id("test", Tags.empty(), null, null, Meter.Type.GAUGE));

        assertThat(id.getTags()).containsExactly(
            Tag.of("cf.account", "test-cf-api"),
            Tag.of("cf.application", "test.application"),
            Tag.of("cf.cluster", "test.application-r042"),
            Tag.of("cf.instance.index", "test-instance-index"),
            Tag.of("cf.space", "test-space-name"),
            Tag.of("cf.version", "42")
        );
    }
}