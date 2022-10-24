package org.tlinks.network.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author : zzh
 * create at:  2022/9/15
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleMqttMessage implements MqttMessage {

    private String topic;

    private String clientId;

    private int qosLevel;

    private ByteBuf payload;

    private int messageId;

    private boolean will;

    private boolean dup;

    private boolean retain;

    private MessagePayloadType payloadType;


    public static SimpleMqttMessageBuilder builder() {
        return new SimpleMqttMessageBuilder();
    }

    public static class SimpleMqttMessageBuilder {
        private String topic;
        private String clientId;
        private int qosLevel;
        private ByteBuf payload;
        private int messageId;
        private boolean will;
        private boolean dup;
        private boolean retain;
        private MessagePayloadType payloadType;

        SimpleMqttMessageBuilder() {
        }

        public SimpleMqttMessageBuilder body(String payload) {
            return payload(payload.getBytes());
        }

        public SimpleMqttMessageBuilder body(byte[] payload) {
            return payload(Unpooled.wrappedBuffer(payload));
        }

        public SimpleMqttMessageBuilder payload(String payload) {
            return payload(payload.getBytes());
        }

        public SimpleMqttMessageBuilder payload(byte[] payload) {
            return payload(Unpooled.wrappedBuffer(payload));
        }

        public SimpleMqttMessageBuilder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public SimpleMqttMessageBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public SimpleMqttMessageBuilder qosLevel(int qosLevel) {
            this.qosLevel = qosLevel;
            return this;
        }

        public SimpleMqttMessageBuilder payload(ByteBuf payload) {
            this.payload = payload;
            return this;
        }

        public SimpleMqttMessageBuilder messageId(int messageId) {
            this.messageId = messageId;
            return this;
        }

        public SimpleMqttMessageBuilder will(boolean will) {
            this.will = will;
            return this;
        }

        public SimpleMqttMessageBuilder dup(boolean dup) {
            this.dup = dup;
            return this;
        }

        public SimpleMqttMessageBuilder retain(boolean retain) {
            this.retain = retain;
            return this;
        }

        public SimpleMqttMessageBuilder payloadType(MessagePayloadType payloadType) {
            this.payloadType = payloadType;
            return this;
        }

        public SimpleMqttMessage build() {
            return new SimpleMqttMessage(topic, clientId, qosLevel, payload, messageId, will, dup, retain, payloadType);
        }

        @Override
        public String toString() {
            return "SimpleMqttMessage.SimpleMqttMessageBuilder(topic=" + this.topic + ", clientId=" + this.clientId + ", qosLevel=" + this.qosLevel + ", payload=" + this.payload + ", messageId=" + this.messageId + ", will=" + this.will + ", dup=" + this.dup + ", retain=" + this.retain + ", payloadType=" + this.payloadType + ")";
        }
    }


    @Override
    public ByteBuf getPayload() {
        return payload;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public int getQosLevel() {
        return qosLevel;
    }

    public void setQosLevel(int qosLevel) {
        this.qosLevel = qosLevel;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public boolean isWill() {
        return will;
    }

    public void setWill(boolean will) {
        this.will = will;
    }

    @Override
    public boolean isDup() {
        return dup;
    }

    public void setDup(boolean dup) {
        this.dup = dup;
    }

    @Override
    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public MessagePayloadType getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(MessagePayloadType payloadType) {
        this.payloadType = payloadType;
    }

}
