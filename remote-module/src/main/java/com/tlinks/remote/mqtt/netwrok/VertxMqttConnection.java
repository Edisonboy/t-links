package com.tlinks.remote.mqtt.netwrok;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : zzh
 * create at:  2022/8/27
 * @description:
 */
public class VertxMqttConnection {

    private long keepAliveTimeoutMs;
    @Getter
    private long lastPingTime = System.currentTimeMillis();

    private volatile boolean closed = false, accepted = false, autoAckSub = true, autoAckUnSub = true, autoAckMsg = true;

    private static final MqttAuth emptyAuth = new MqttAuth() {
        @Override
        public String getUsername() {
            return "";
        }

        @Override
        public String getPassword() {
            return "";
        }
    };





    @AllArgsConstructor
    class VertxMqttPublishing implements MqttPublishing {

        @Override
        public MqttMessage getMessage() {
            return null;
        }

        @Override
        public void acknowledge() {

        }
    }
}
