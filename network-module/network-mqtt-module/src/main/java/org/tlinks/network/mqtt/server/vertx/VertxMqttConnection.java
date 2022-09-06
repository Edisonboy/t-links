package org.tlinks.network.mqtt.server.vertx;

import io.vertx.mqtt.MqttEndpoint;
import lombok.Getter;
import org.tlinks.network.mqtt.server.MqttConnection;

/**
 * @author : zzh
 * create at:  2022/9/6
 * @description:
 */
public class VertxMqttConnection implements MqttConnection {

    private final MqttEndpoint endpoint;
    private long keepAliveTimeoutMs;
    @Getter
    private final long lastPingTime = System.currentTimeMillis();

    public VertxMqttConnection(MqttEndpoint endpoint) {
        this.endpoint = endpoint;
    }
}
