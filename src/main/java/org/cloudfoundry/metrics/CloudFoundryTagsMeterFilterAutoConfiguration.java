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

import com.netflix.frigga.Names;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

@AutoConfigureBefore(MetricsAutoConfiguration.class)
@ConditionalOnClass(MeterFilter.class)
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
@Configuration
class CloudFoundryTagsMeterFilterAutoConfiguration {

    @Bean
    @Order
    MeterFilter meterFilter(Environment environment) {
        Names names = getNames(environment);

        return new CloudFoundryTagsMeterFilter(
            getAccount(environment),
            getApplication(environment, names),
            getCluster(environment, names),
            getInstanceIndex(environment),
            getOrganization(environment),
            getSpace(environment),
            getVersion(environment, names)
        );
    }

    private String getAccount(Environment environment) {
        String account = environment.getProperty("cf.app.account");
        if (account != null) {
            return account;
        }

        return environment.getProperty("vcap.application.cf_api");
    }

    private String getApplication(Environment environment, Names names) {
        String application = environment.getProperty("cf.app.application");
        if (application != null) {
            return application;
        }

        return names.getApp();
    }

    private String getCluster(Environment environment, Names names) {
        String cluster = environment.getProperty("cf.app.cluster");
        if (cluster != null) {
            return cluster;
        }

        return names.getCluster();
    }

    private String getInstanceIndex(Environment environment) {
        String instanceIndex = environment.getProperty("cf.app.instance.index");
        if (instanceIndex != null) {
            return instanceIndex;
        }

        return environment.getProperty("cf.instance.index");
    }

    private Names getNames(Environment environment) {
        return Names.parseName(environment.getProperty("vcap.application.application_name"));
    }

    private String getOrganization(Environment environment) {
        String organization = environment.getProperty("cf.app.organization");
        if (organization != null) {
            return organization;
        }

        return environment.getProperty("vcap.application.organization_name");
    }

    private String getSpace(Environment environment) {
        String space = environment.getProperty("cf.app.space");
        if (space != null) {
            return space;
        }

        return environment.getProperty("vcap.application.space_name");
    }

    private String getVersion(Environment environment, Names names) {
        String version = environment.getProperty("cf.app.version");
        if (version != null) {
            return version;
        }

        return names.getRevision();
    }
}
