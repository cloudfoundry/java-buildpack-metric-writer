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

package org.cloudfoundry.metron;

import com.google.protobuf.MessageLite;
import org.cloudfoundry.dropsonde.events.MetricFactory;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@ExportMetricWriter
final class MetronMetricWriter extends Endpoint implements MetricWriter {

    private final AtomicReference<Optional<Session>> session = new AtomicReference<>(Optional.empty());

    MetronMetricWriter() {
    }

    MetronMetricWriter(URI uri) throws IOException, DeploymentException {
        ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
    }

    @Override
    public void increment(Delta<?> delta) {
        send(this.session, MetricFactory.CounterEvent.newBuilder()
            .setName(delta.getName())
            .setDelta(delta.getValue().longValue())
            .build());
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        this.session.set(Optional.empty());
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session.set(Optional.of(session));
    }

    @Override
    public void reset(String metricName) {
        send(this.session, MetricFactory.ValueMetric.newBuilder()
            .setName(metricName)
            .setValue(0)
            .setUnit("")
            .build());
    }

    @Override
    public void set(Metric<?> value) {
        send(this.session, MetricFactory.ValueMetric.newBuilder()
            .setName(value.getName())
            .setValue(value.getValue().longValue())
            .setUnit("")
            .build());
    }

    private static void send(AtomicReference<Optional<Session>> sessionReference, MessageLite message) {
        sessionReference.get()
            .ifPresent(session -> session.getAsyncRemote().sendBinary(ByteBuffer.wrap(message.toByteArray())));
    }

}
