package org.tlinks.network.mqtt;

/**
 * @author : zzh
 * create at:  2022/9/9
 * @description:
 */
public interface MqttPublishMessage extends MqttMessage {

    void acknowledge();
}
