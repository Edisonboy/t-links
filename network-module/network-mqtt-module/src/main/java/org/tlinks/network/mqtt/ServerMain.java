package org.tlinks.network.mqtt;

import io.netty.buffer.Unpooled;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;
import org.tlinks.network.message.MqttMessage;
import org.tlinks.network.message.SimpleMqttMessage;
import org.tlinks.network.mqtt.server.vertx.VertxMqttServer;
import org.tlinks.network.mqtt.server.vertx.VertxMqttServerProperties;
import org.tlinks.network.mqtt.server.vertx.VertxMqttServerProvider;
import reactor.core.scheduler.Schedulers;

/**
 * @author : zzh
 * create at:  2022/9/6
 * @description:
 */
public class ServerMain {


    public static void main(String[] args) {
        VertxMqttServerProvider provider = new VertxMqttServerProvider(Vertx.vertx());

        VertxMqttServerProperties properties = new VertxMqttServerProperties();
        properties.setId("test");
        properties.setOptions(new MqttServerOptions());

        VertxMqttServer network = provider.createNetwork(properties);

        network
                .acquireConnection()
                .doOnNext(mqttConnection -> {
                    // TODO 为什么每有一个新的客户端进来，就会执行这里
                    System.out.println(mqttConnection.getClientId());
                    // 开启服务
                    mqttConnection.accept();

                    mqttConnection.handleMessage()
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(msg -> {
                                System.out.println("===handle message===");
                                System.out.println(msg.payloadAsString());

                                msgHandler(mqttConnection, msg);
                                System.out.println("==========================");
                            })
                            .subscribe();

                    mqttConnection.handleSubscribe(false)
                            .doOnNext(subscription -> {
                                System.out.println("===handle subscription===");
                                int size = subscription.getMessage().topicSubscriptions().size();
                                System.out.println("size:" + size);
                                for (MqttTopicSubscription sub : subscription.getMessage().topicSubscriptions()) {
                                    System.out.println("topic name:" + sub.topicName());
                                }
                                System.out.println("==========================");
                            })
                            .subscribe();

                    mqttConnection.handleUnSubscribe(false)
                            .doOnNext(mqttUnSubscription -> {
                                System.out.println("===handle unSubscription===");
                                System.out.println(mqttUnSubscription.getMessage().properties().toString());
                                System.out.println("==========================");
                            })
                            .subscribe();
                })
                //.flatMap(mqttConnection -> mqttConnection)
                .subscribe();
    }

    public static void msgHandler(MqttConnection connection, MqttMessage message) {
        System.out.println("send msg");
        connection.publish(SimpleMqttMessage.builder()
                        .qosLevel(2)
                        .topic("/test")
                        .payload(Unpooled.wrappedBuffer("testhahah".getBytes()))
                        .build())
                //.doOnSubscribe()
                .subscribe();
    }
    
}
