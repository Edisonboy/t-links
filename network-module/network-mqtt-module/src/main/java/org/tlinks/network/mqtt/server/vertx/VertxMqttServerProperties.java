package org.tlinks.network.mqtt.server.vertx;

import io.vertx.mqtt.MqttServerOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : zzh
 * create at:  2022/9/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertxMqttServerProperties {

    private String id;

    /**
     * 服务实例数量(线程数)
     */
    private int instance = 4;

    /**
     * 证书ID
     */
    private String certId;

    private boolean ssl;

    private MqttServerOptions options;
}
