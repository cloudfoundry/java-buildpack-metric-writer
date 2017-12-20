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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public final class RestOperationsMetricPublisherTest {

    private static final String ACCESS_TOKEN = "test-access-token";

    private static final String APPLICATION_ID = "test-application-id";

    private static final String INSTANCE_ID = "test-instance-id";

    private static final String INSTANCE_INDEX = "test-instance-index";

    private final CloudFoundryMetricWriterProperties properties = new CloudFoundryMetricWriterProperties(ACCESS_TOKEN, APPLICATION_ID, "https://localhost/v1/metrics", INSTANCE_ID, INSTANCE_INDEX, -1, false);

    private final RestTemplate restTemplate = new RestTemplate();

    private final MockRestServiceServer mockServer = MockRestServiceServer.bindTo(this.restTemplate).build();

    private final RestOperationsMetricPublisher metricPublisher = new RestOperationsMetricPublisher(this.properties, this.restTemplate);

    @Test
    public void announce() {
        this.metricPublisher.announce();
    }

    @Test
    public void empty() {
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

        this.metricPublisher.publish(Collections.emptyList());

        this.mockServer.verify();
    }

    @Test
    public void otherFailure() {
        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isNotEmpty())
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        this.metricPublisher.publish(Collections.singletonList(getMetric(0)));

        this.mockServer.verify();

        this.mockServer.reset();

        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isNotEmpty())
            .andRespond(withSuccess());

        this.metricPublisher.publish(Collections.emptyList());

        this.mockServer.verify();
    }

    @Test
    public void payloadTooLarge() {
        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isNotEmpty())
            .andRespond(withStatus(HttpStatus.PAYLOAD_TOO_LARGE));

        this.metricPublisher.publish(Collections.singletonList(getMetric(0)));

        this.mockServer.verify();

        this.mockServer.reset();

        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isEmpty())
            .andRespond(withSuccess());

        this.metricPublisher.publish(Collections.emptyList());

        this.mockServer.verify();
    }

    @Test
    public void tooManyRequests() {
        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isNotEmpty())
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        this.metricPublisher.publish(Collections.singletonList(getMetric(0)));

        this.mockServer.verify();

        this.mockServer.reset();

        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isNotEmpty())
            .andRespond(withSuccess());

        this.metricPublisher.publish(Collections.emptyList());

        this.mockServer.verify();
    }

    @Test
    public void unprocessableEntity() {
        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isNotEmpty())
            .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        this.metricPublisher.publish(Collections.singletonList(getMetric(0)));

        this.mockServer.verify();

        this.mockServer.reset();

        this.mockServer
            .expect(method(HttpMethod.POST)).andExpect(requestTo("https://localhost/v1/metrics"))
            .andExpect(jsonPath("$.applications[0].instances[0].metrics").isEmpty())
            .andRespond(withSuccess());

        this.metricPublisher.publish(Collections.emptyList());

        this.mockServer.verify();
    }

    @Test
    public void values() throws InterruptedException, IOException {
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

        this.metricPublisher.publish(Arrays.asList(getMetric(0), getMetric(1), getMetric(2), getMetric(3)));

        this.mockServer.verify();
    }

    private static Metric getMetric(int index) {
        return new Metric(getName(index), Collections.emptyMap(), new Date(index).getTime(), Type.GAUGE, null, index);
    }

    private static String getName(int index) {
        return String.format("test-metric-%d", index);
    }

}
