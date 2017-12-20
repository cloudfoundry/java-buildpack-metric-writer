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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The properties for configuring a metric writer
 */
@ConfigurationProperties(prefix = "cloudfoundry.metrics")
public final class CloudFoundryMetricWriterProperties {

    private String accessToken;

    private String applicationId;

    private String endpoint;

    private String instanceId;

    private String instanceIndex;

    private long rate = 60_000;

    private boolean skipSslValidation = false;

    CloudFoundryMetricWriterProperties() {
    }

    public CloudFoundryMetricWriterProperties(String accessToken, String applicationId, String endpoint, String instanceId, String instanceIndex, long rate, boolean skipSslValidation) {
        this.accessToken = accessToken;
        this.applicationId = applicationId;
        this.endpoint = endpoint;
        this.instanceId = instanceId;
        this.instanceIndex = instanceIndex;
        this.rate = rate;
        this.skipSslValidation = skipSslValidation;
    }

    String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    String getInstanceId() {
        return this.instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    String getInstanceIndex() {
        return this.instanceIndex;
    }

    public void setInstanceIndex(String instanceIndex) {
        this.instanceIndex = instanceIndex;
    }

    public long getRate() {
        return this.rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }

    boolean isSkipSslValidation() {
        return this.skipSslValidation;
    }

    public void setSkipSslValidation(boolean skipSslValidation) {
        this.skipSslValidation = skipSslValidation;
    }
}
