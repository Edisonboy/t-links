package org.tlinks.network.mqtt;

import io.vertx.mqtt.messages.MqttSubscribeMessage;

/**
 * @author : zzh
 * create at:  2022/9/8
 * @description:
 */
public interface MqttSubscription {

    MqttSubscribeMessage getMessage();

    void acknowledge();
}
