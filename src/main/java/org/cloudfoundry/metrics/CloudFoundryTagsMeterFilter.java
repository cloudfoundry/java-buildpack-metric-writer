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
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.core.env.Environment;

final class CloudFoundryTagsMeterFilter implements MeterFilter {

    private static final String ACCOUNT = "cf.account";

    private static final String APPLICATION = "cf.application";

    private static final String CLUSTER = "cf.cluster";

    private static final String INSTANCE_INDEX = "cf.instance.index";

    private static final String ORGANIZATION = "cf.organization";

    private static final String SPACE = "cf.space";

    private static final String VERSION = "cf.version";

    private final Tag account;

    private final Tag application;

    private final Tag cluster;

    private final Tag instanceIndex;

    private final Tag organization;

    private final Tag space;

    private final Tag version;

    CloudFoundryTagsMeterFilter(String account, String application, String cluster, String instanceIndex, String organization, String space, String version) {
        this.account = account != null ? Tag.of(ACCOUNT, account) : null;
        this.application = application != null ? Tag.of(APPLICATION, application) : null;
        this.cluster = cluster != null ? Tag.of(CLUSTER, cluster) : null;
        this.instanceIndex = instanceIndex != null ? Tag.of(INSTANCE_INDEX, instanceIndex) : null;
        this.organization = organization != null ? Tag.of(ORGANIZATION, organization) : null;
        this.space = space != null ? Tag.of(SPACE, space) : null;
        this.version = version != null ? Tag.of(VERSION, version) : null;
    }

    @Override
    public Meter.Id map(Meter.Id id) {
        Meter.Id i = id;

        i = addIfMissing(i, ACCOUNT, this.account);
        i = addIfMissing(i, APPLICATION, this.application);
        i = addIfMissing(i, CLUSTER, this.cluster);
        i = addIfMissing(i, INSTANCE_INDEX, this.instanceIndex);
        i = addIfMissing(i, ORGANIZATION, this.organization);
        i = addIfMissing(i, SPACE, this.space);
        i = addIfMissing(i, VERSION, this.version);

        return i;
    }

    private Meter.Id addIfMissing(Meter.Id id, String key, Tag tag) {
        if (tag != null && id.getTag(key) == null) {
            return id.withTag(tag);
        }

        return id;
    }
}
