package com.tlinks.remote.sample;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : zzh
 * create at:  2022/8/26
 * @description:
 */
@Slf4j
public class MyMqttClient {

    public static final String MQTT_TOPIC = "hello_topic";
    public static Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {
        MqttClient client = MqttClient.create(vertx);

        client
            .connect(1883, "127.0.0.1", s -> {
                if (s.succeeded()) {
                    log.info("Client connect success.");
                    subscribe(client);
                } else {
                    log.error("Client connect fail: ", s.cause());
                }
            })
            .exceptionHandler(event -> {
                log.error("client fail: ", event.getCause());
            })
            .publishHandler(msg -> {


            })


        ;
    }

    private static void subscribe(MqttClient client) {
        client.subscribe(MQTT_TOPIC, 0, e -> {
            if (e.succeeded()) {
                log.info("===>subscribe success: {}", e.result());
                vertx.setPeriodic(10_000, l -> publish(client));
            } else {
                log.error("===>subscribe fail: ", e.cause());
            }
        });
    }


    private static void publish(MqttClient client) {
        Buffer payload = Buffer.buffer("client: hello world.");
        client.publish(MQTT_TOPIC, payload, MqttQoS.AT_MOST_ONCE, false, false, s -> {
            if (s.succeeded()) {
                log.info("===>Client publish success: {}", s.result());
            } else {
                log.error("===>Client publish fail: ", s.cause());
            }
        });
    }

}
