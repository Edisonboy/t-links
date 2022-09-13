package org.tlinks.network.mqtt;

/**
 * @author : zzh
 * create at:  2022/9/7
 * @description:
 */
public interface MqttMessage extends EncodeMessage{

    String getTopic();

    String clientId();

    int messageId();

    default boolean isWill() {
        return false;
    }

    default int getQosLevel() {
        return 0;
    }

    default boolean isDup() {
        return false;
    }

    default boolean isRetain() {
        return false;
    }


}
