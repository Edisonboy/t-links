package org.tlinks.network.mqtt.server.vertx;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tlinks.network.mqtt.MqttMessage;
import org.tlinks.network.mqtt.MqttSubscription;
import org.tlinks.network.mqtt.MqttUnSubscription;
import org.tlinks.network.mqtt.MqttConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author : zzh
 * create at:  2022/9/6
 * @description:
 */
@Slf4j
public class VertxMqttConnection implements MqttConnection {

    private final MqttEndpoint endpoint;
    private long keepAliveTimeoutMs;
    private volatile boolean closed = false, accepted = false, autoAckSub = true, autoAckUnSub = true, autoAckMsg = true;
    @Getter
    private long lastPingTime = System.currentTimeMillis();

    public VertxMqttConnection(MqttEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    private final Sinks.Many<MqttMessage> messageProcessor = Sinks
            .many()
            .multicast()
            .onBackpressureBuffer(Integer.MAX_VALUE);

    private final Sinks.Many<MqttSubscription> subscription = Sinks
            .many()
            .multicast()
            .onBackpressureBuffer(Integer.MAX_VALUE);

    private final Sinks.Many<MqttUnSubscription> unsubscription = Sinks
            .many()
            .multicast()
            .onBackpressureBuffer(Integer.MAX_VALUE);

    private final Consumer<MqttConnection> defaultListener = mqttConnection -> {
        VertxMqttConnection.log.debug("mqtt client [{}] disconnected", getClientId());
        subscription.tryEmitComplete();
        unsubscription.tryEmitComplete();
        messageProcessor.tryEmitComplete();
    };

    /**
     * 自定义断联的处理方式
     */
    private Consumer<MqttConnection> disconnectConsumer = defaultListener;

    @Override
    public void onClose(Consumer<MqttConnection> listener) {
        disconnectConsumer = disconnectConsumer.andThen(listener);
    }

    @Override
    public Mono<Void> publish(MqttMessage message) {
        ping();
        return Mono.create(sink -> {
            Buffer buffer = Buffer.buffer(message.getPayload());
            endpoint.publish(
                    message.getTopic(),
                    buffer,
                    MqttQoS.valueOf(message.getQosLevel()),
                    message.isDup(),
                    message.isRetain(),
                    result -> {
                        if (result.succeeded()) {
                            sink.success();
                        } else {
                            sink.error(result.cause());
                        }
                    }
            );
        });
    }

    @Override
    public String getClientId() {
        return endpoint.clientIdentifier();
    }

    @Override
    public void reject(MqttConnectReturnCode code) {
        if (closed) {
            return;
        }
        try {
            endpoint.reject(code);
        } catch (Throwable ignore) {
        }
        try {
            complete();
        } catch (Throwable ignore) {

        }
    }

    private void complete() {
        if (closed) {
            return;
        }
        closed = true;
        disconnectConsumer.accept(this);
    }

    @Override
    public MqttConnection accept() {
        if (accepted) {
            return this;
        }
        log.debug("mqtt client [{}] connected", getClientId());
        accepted = true;
        try {
            if (!endpoint.isConnected()) {
                endpoint.accept();
            }
        } catch (Exception e) {
            close().subscribe();
            log.warn(e.getMessage(), e);
            return this;
        }
        // TODO 可以在调用 accept() 方法之后再对 endpoint 设置 handler
        init();
        return this;
    }

    @Override
    public Mono<Void> close() {
        if (closed) {
            return Mono.empty();
        }
        return Mono.fromRunnable(() -> {
            try {
                if (endpoint.isConnected()) {
                    endpoint.close();
                } else {
                    complete();
                }
            } catch (Throwable ignore) {
            }
        });
    }

    void init() {
        this.endpoint
                .disconnectHandler(ignore -> this.complete())
                .closeHandler(ignore -> this.complete())
                .exceptionHandler(error -> {
                    if (error instanceof DecoderException) {
                        if (error.getMessage().contains("too large message")) {
                            log.error("MQTT消息过大,请在网络组件中设置[最大消息长度].", error);
                            return;
                        }
                    }
                    log.error(error.getMessage(), error);
                })
                .pingHandler(ignore -> {
                    this.ping();
                    // TODO 如果是非自动保持连接，需要发送ping?
                    if (!endpoint.isAutoKeepAlive()) {
                        endpoint.pong();
                    }
                })
                .publishHandler(msg -> {
                    ping();
                    VertxMqttPublishMessage publishMsg = new VertxMqttPublishMessage(msg, false);

                    boolean hasDownstream = this.messageProcessor.currentSubscriberCount() > 0;
                    // TODO 返回ack
                    if (autoAckMsg && hasDownstream) {
                        publishMsg.acknowledge();
                    }
                    if (hasDownstream) {
                        this.messageProcessor.tryEmitNext(publishMsg);
                    }
                })
                //QoS 1 PUBACK
                .publishAcknowledgeHandler(messageId -> {
                    ping();
                    log.debug("PUBACK mqtt[{}] message[{}]", getClientId(), messageId);
                })
                //QoS 2  PUBREC
                .publishReceivedHandler(messageId -> {
                    ping();
                    log.debug("PUBREC mqtt[{}] message[{}]", getClientId(), messageId);
                    endpoint.publishRelease(messageId);
                })
                //QoS 2  PUBREL
                .publishReleaseHandler(messageId -> {
                    ping();
                    log.debug("PUBREL mqtt[{}] message[{}]", getClientId(), messageId);
                    endpoint.publishComplete(messageId);
                })
                //QoS 2  PUBCOMP
                .publishCompletionHandler(messageId -> {
                    ping();
                    log.debug("PUBCOMP mqtt[{}] message[{}]", getClientId(), messageId);
                })
                .subscribeHandler(msg -> {
                    ping();
                    VertxMqttSubscription subscription = new VertxMqttSubscription(msg, false);
                    boolean hasDownstream = this.subscription.currentSubscriberCount() > 0;
                    if (autoAckSub || !hasDownstream) {
                        subscription.acknowledge();
                    }
                    if (hasDownstream) {
                        this.subscription.tryEmitNext(subscription);
                    }
                })
                .unsubscribeHandler(msg -> {
                    ping();
                    VertxMqttMqttUnSubscription unSubscription = new VertxMqttMqttUnSubscription(msg, false);
                    boolean hasDownstream = this.unsubscription.currentSubscriberCount() > 0;
                    if (autoAckUnSub || !hasDownstream) {
                        unSubscription.acknowledge();
                    }
                    if (hasDownstream) {
                        this.unsubscription.tryEmitNext(unSubscription);
                    }
                });
    }

    void ping() {
        lastPingTime = System.currentTimeMillis();
    }

    @Override
    public Flux<MqttMessage> handleMessage() {
        return messageProcessor.asFlux();
    }

    @Override
    public Flux<MqttSubscription> handleSubscribe(boolean autoAck) {
        return subscription.asFlux();
    }

    @Override
    public Flux<MqttUnSubscription> handleUnSubscribe(boolean autoAck) {
        return unsubscription.asFlux();
    }

    @AllArgsConstructor
    class VertxMqttSubscription implements MqttSubscription {
        private final MqttSubscribeMessage message;

        private volatile boolean acknowledged;


        @Override
        public MqttSubscribeMessage getMessage() {
            return message;
        }

        @Override
        public synchronized void acknowledge() {
            if (acknowledged) {
                return;
            }
            acknowledged = true;
            endpoint.subscribeAcknowledge(message.messageId(), message
                    .topicSubscriptions()
                    .stream()
                    .map(MqttTopicSubscription::qualityOfService)
                    .collect(Collectors.toList()));
        }
    }

    @AllArgsConstructor
    class VertxMqttMqttUnSubscription implements MqttUnSubscription {

        private final MqttUnsubscribeMessage message;

        private volatile boolean acknowledged;

        @Override
        public MqttUnsubscribeMessage getMessage() {
            return message;
        }

        @Override
        public synchronized void acknowledge() {
            if (acknowledged) {
                return;
            }
            log.info("acknowledge mqtt [{}] unsubscribe : {} ", getClientId(), message.topics());
            acknowledged = true;
            endpoint.unsubscribeAcknowledge(message.messageId());
        }
    }


    @AllArgsConstructor
    class VertxMqttPublishMessage implements MqttMessage {

        private final MqttPublishMessage message;

        private volatile boolean acknowledged;

        /**
         * ack 返回给client
         */
        public void acknowledge() {
            if (acknowledged) {
                return;
            }
            acknowledged = true;
            if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                log.debug("PUBACK QoS1 mqtt[{}] message[{}]", getClientId(), message.messageId());
                endpoint.publishAcknowledge(message.messageId());
            } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                log.debug("PUBREC QoS2 mqtt[{}] message[{}]", getClientId(), message.messageId());
                endpoint.publishReceived(message.messageId());
            }
        }

        @Override
        public String getTopic() {
            return message.topicName();
        }

        @Override
        public String clientId() {
            return VertxMqttConnection.this.getClientId();
        }

        @Override
        public int messageId() {
            return message.messageId();
        }

        @Nonnull
        @Override
        public ByteBuf getPayload() {
            return message.payload().getByteBuf();
        }
    }
}
