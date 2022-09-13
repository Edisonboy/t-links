package org.tlinks.network.mqtt;

import io.vertx.mqtt.messages.MqttUnsubscribeMessage;

public interface MqttUnSubscription {

    MqttUnsubscribeMessage getMessage();

    void acknowledge();

}
