/*
 * Copyright 2016 the original author or authors.
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

package org.cloudfoundry.metricwriter;

import com.google.protobuf.MessageLite;
import org.cloudfoundry.dropsonde.events.MetricFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.Delta;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import java.nio.ByteBuffer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public final class MetronMetricWriterTest {

    private final Async async = mock(Async.class);

    private final MetronMetricWriter metricWriter = new MetronMetricWriter();

    private final Session session = mock(Session.class);

    @Test
    public void increment() {
        this.metricWriter.onOpen(this.session, null);
        this.metricWriter.increment(new Delta<>("test-name", Long.MIN_VALUE));

        verify(this.async, MetricFactory.CounterEvent.newBuilder()
            .setName("test-name")
            .setDelta(Long.MIN_VALUE)
            .build());
    }

    @Test
    public void incrementNoSession() {
        this.metricWriter.increment(new Delta<Number>("test-name", Long.MIN_VALUE));

        verifyZeroInteractions(this.async);
    }

    @Test
    public void onClose() {
        this.metricWriter.onOpen(this.session, null);
    }

    @Test
    public void onError() {
        this.metricWriter.onError(null, new Exception());
    }

    @Test
    public void onOpen() {
        this.metricWriter.onOpen(this.session, null);
    }

    @Test
    public void reset() {
        this.metricWriter.onOpen(this.session, null);
        this.metricWriter.reset("test-name");

        verify(this.async, MetricFactory.ValueMetric.newBuilder()
            .setName("test-name")
            .setValue(0)
            .setUnit("")
            .build());
    }

    @Test
    public void resetNoSession() {
        this.metricWriter.reset("test-name");

        verifyZeroInteractions(this.async);
    }

    @Test
    public void set() {
        this.metricWriter.onOpen(this.session, null);
        this.metricWriter.set(new Metric<>("test-name", Long.MIN_VALUE));

        verify(this.async, MetricFactory.ValueMetric.newBuilder()
            .setName("test-name")
            .setValue(Long.MIN_VALUE)
            .setUnit("")
            .build());
    }

    @Test
    public void setNoSession() {
        this.metricWriter.set(new Metric<>("test-name", Long.MIN_VALUE));

        verifyZeroInteractions(this.async);
    }

    @Before
    public void setUp() throws Exception {
        when(this.session.getAsyncRemote()).thenReturn(this.async);
    }

    private static void verify(Async async, MessageLite message) {
        Mockito.verify(async).sendBinary(ByteBuffer.wrap(message.toByteArray()));
    }

}
