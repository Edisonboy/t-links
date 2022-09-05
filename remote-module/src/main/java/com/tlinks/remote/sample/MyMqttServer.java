package com.tlinks.remote.sample;

import io.netty.handler.codec.DecoderException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : zzh
 * create at:  2022/8/25
 * @description:
 */
@Slf4j
public class MyMqttServer {

    public static void main(String[] args) {
        MyMqttServer myMqttServer = new MyMqttServer();
        myMqttServer.createServer();
    }



    public void createServer() {
        MqttServer mqttServer = MqttServer.create(Vertx.vertx());
        mqttServer
                .exceptionHandler(
                        t -> log.error("MQTT exception fail: ", t)
                )
                .endpointHandler(endpoint -> {
                    // shows main connect info
                    System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

                    if (endpoint.auth() != null) {
                        System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
                    }
                    //System.out.println("[properties = " + endpoint.connectProperties() + "]");
                    if (endpoint.will() != null) {
                        System.out.println(endpoint.will().toJson().toString());
                        System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) + " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
                    }

                    System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

                    // accept connection from the remote client
                    endpoint.accept(false);

                    endpoint
                            .disconnectHandler(ignore -> {
                                log.info("disconnect msg");
                                this.complete();
                            })
                            .closeHandler(ignore -> {
                                log.info("close msg");
                                this.complete();
                            })
                            // TODO endpoint的exception与外层的exception有什么区别
                            .exceptionHandler(error -> {
                                if (error instanceof DecoderException) {
                                    if (error.getMessage().contains("too large message")) {
                                        log.error("MQTT消息过大,请在网络组件中设置[最大消息长度].", error);
                                        return;
                                    }
                                }
                                log.error(error.getMessage(), error);
                            })
                            // TODO 接收客户端发送的心跳？
                            .pingHandler(handle -> {
                                log.info("ping msg");
                                this.ping();
                                if (!endpoint.isAutoKeepAlive()) {
                                    endpoint.pong();
                                }
                            })
                            .publishHandler(msg -> {

                                log.info("publish msg:{}", msg.toString());
                                System.out.println(msg.payload().toString());
                                ping();
                                /*VertxMqttPublishing publishing = new VertxMqttPublishing(msg, false);
                                // TODO 当前消费者数，这个processor的消费者在哪里设置，即订阅
                                boolean hasDownstream = this.messageProcessor.currentSubscriberCount() > 0;
                                // 返回ack
                                if (autoAckMsg && hasDownstream) {
                                    publishing.acknowledge();
                                }
                                if (hasDownstream) {
                                    this.messageProcessor.tryEmitNext(publishing);
                                }*/
                                //log.info();
                            })
                            //QoS 1 PUBACK  发布确认
                            .publishAcknowledgeHandler(messageId -> {
                                log.info("publish ack msg");
                                ping();
                                log.debug("PUBACK mqtt[{}] message[{}]", endpoint.clientIdentifier(), messageId);
                            })
                            //QoS 2  PUBREC    发布的消息已接收
                            .publishReceivedHandler(messageId -> {
                                log.info("publish received msg");
                                ping();
                                log.debug("PUBREC mqtt[{}] message[{}]", endpoint.clientIdentifier(), messageId);
                                endpoint.publishRelease(messageId);
                            })
                            //QoS 2  PUBREL    发布的消息已释放
                            .publishReleaseHandler(messageId -> {
                                log.info("publish release msg");
                                ping();
                                log.debug("PUBREL mqtt[{}] message[{}]", endpoint.clientIdentifier(), messageId);
                                endpoint.publishComplete(messageId);
                            })
                            //QoS 2  PUBCOMP  发布完成
                            .publishCompletionHandler(messageId -> {
                                log.info("publish completion msg");
                                ping();
                                log.debug("PUBCOMP mqtt[{}] message[{}]", endpoint.clientIdentifier(), messageId);
                            })
                            // 订阅请求
                            .subscribeHandler(msg -> {
                                log.info("subscribe msg");
                                ping();
                                /*VertxMqttSubscription subscription = new VertxMqttSubscription(msg, false);
                                boolean hasDownstream = this.subscription.currentSubscriberCount() > 0;
                                if (autoAckSub || !hasDownstream) {
                                    subscription.acknowledge();
                                }
                                if (hasDownstream) {
                                    this.subscription.tryEmitNext(subscription);
                                }*/
                            })
                            // 取消订阅
                            .unsubscribeHandler(msg -> {
                                log.info("unsubscribe msg");
                                ping();
                                /*VertxMqttMqttUnSubscription unSubscription = new VertxMqttMqttUnSubscription(msg, false);
                                boolean hasDownstream = this.unsubscription.currentSubscriberCount() > 0;
                                if (autoAckUnSub || !hasDownstream) {
                                    unSubscription.acknowledge();
                                }
                                if (hasDownstream) {
                                    this.unsubscription.tryEmitNext(unSubscription);
                                }*/
                            });


                })
                // TODO 是不是调用了 accept() 才会被调用？？？
                .listen(ar -> {
                    if (ar.succeeded()) {
                        System.out.println("MQTT server is listening on port " + ar.result().actualPort());
                    } else {
                        System.out.println("Error on starting the server");
                        ar.cause().printStackTrace();
                    }
                });
    }

    private void complete() {
        /*if (closed) {
            return;
        }
        closed = true;
        disconnectConsumer.accept(this);*/
    }

    void ping() {
        //lastPingTime = System.currentTimeMillis();
    }



}
