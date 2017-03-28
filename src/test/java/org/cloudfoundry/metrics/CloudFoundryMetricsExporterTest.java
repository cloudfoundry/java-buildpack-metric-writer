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

import org.junit.Test;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public final class CloudFoundryMetricsExporterTest {

    private static final String ACCESS_TOKEN = "test-access-token";

    private static final String APPLICATION_ID = "test-application-id";

    private static final String INSTANCE_ID = "test-instance-id";

    private static final String INSTANCE_INDEX = "test-instance-index";

    private final List<PublicMetrics> metricsCollections = new ArrayList<>();

    private final CloudFoundryMetricsProperties properties = new CloudFoundryMetricsProperties(ACCESS_TOKEN, APPLICATION_ID, "https://localhost", INSTANCE_ID, INSTANCE_INDEX, 60_000, false);

    private final RestTemplate restTemplate = new RestTemplate();

    private final MockRestServiceServer mockServer = MockRestServiceServer.bindTo(this.restTemplate).build();

    private final ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class, RETURNS_SMART_NULLS);

    private final CloudFoundryMetricsExporter exporter = new CloudFoundryMetricsExporter(this.metricsCollections, this.properties, this.restTemplate, this.scheduledExecutorService);

    @Test
    public void empty() throws InterruptedException, IOException {
        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.applications[0].id").value(APPLICATION_ID))
            .andExpect(jsonPath("$.applications[0].instances[0].id").value(INSTANCE_ID))
            .andExpect(jsonPath("$.applications[0].instances[0].index").value(INSTANCE_INDEX))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isEmpty())
            .andRespond(withSuccess());

        this.exporter.run();

        this.mockServer.verify();

    }

    @Test
    public void values() throws InterruptedException, IOException {
        this.metricsCollections.add(new StubPublicMetrics(getMetric(0), getMetric(1)));
        this.metricsCollections.add(new StubPublicMetrics(getMetric(2), getMetric(3)));

        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.applications[0].id").value(APPLICATION_ID))
            .andExpect(jsonPath("$.applications[0].instances[0].id").value(INSTANCE_ID))
            .andExpect(jsonPath("$.applications[0].instances[0].index").value(INSTANCE_INDEX))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[0].name").value(getName(0)))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[0].type").value("gauge"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[0].timestamp").isNumber())
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[0].value").value(0))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[0].unit").doesNotExist())
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[1].name").value(getName(1)))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[1].type").value("gauge"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[1].timestamp").isNumber())
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[1].value").value(1))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[1].unit").doesNotExist())
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[2].name").value(getName(2)))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[2].type").value("gauge"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[2].timestamp").isNumber())
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[2].value").value(2))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[2].unit").doesNotExist())
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[3].name").value(getName(3)))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[3].type").value("gauge"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[3].timestamp").isNumber())
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[3].value").value(3))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics[3].unit").doesNotExist())
            .andRespond(withSuccess());

        this.exporter.run();

        this.mockServer.verify();
    }

    private static org.springframework.boot.actuate.metrics.Metric<?> getMetric(int index) {
        return new org.springframework.boot.actuate.metrics.Metric<>(getName(index), index, new Date(index));
    }

    private static String getName(int index) {
        return String.format("test-metric-%d", index);
    }

}
