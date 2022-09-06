package org.tlinks.network.mqtt.server;

import org.tlinks.network.Network;
import reactor.core.publisher.Flux;

/**
 * @author : zzh
 * create at:  2022/9/2
 * @description:
 */
public interface MqttServer extends Network {

    /**
     * 获取端连接
     *
     * @return 获取端连接
     */
    Flux<MqttConnection> acquireConnection();
}
