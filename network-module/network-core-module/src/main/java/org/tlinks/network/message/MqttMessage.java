package org.tlinks.network.message;

/**
 * @author : zzh
 * create at:  2022/9/7
 * @description:
 */
public interface MqttMessage extends EncodeMessage {

    /**
     * @return topic
     */
    String getTopic();

    /**
     * @return clientId
     */
    String getClientId();

    /**
     * @return messageId
     */
    int getMessageId();

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
