package org.tlinks.network.mqtt.server.vertx;

import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import lombok.extern.slf4j.Slf4j;
import org.tlinks.network.Network;
import org.tlinks.network.NetworkProperties;
import org.tlinks.network.NetworkProvider;
import org.tlinks.network.NetworkType;
import org.tlinks.network.metadata.ConfigMetadata;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author : zzh
 * create at:  2022/9/6
 * @description:
 */
@Slf4j
public class VertxMqttServerProvider implements NetworkProvider<VertxMqttServerProperties> {

    private final Vertx vertx;

    public VertxMqttServerProvider(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public NetworkType getType() {
        return null;
    }

    @Override
    public Network createNetwork(VertxMqttServerProperties properties) {
        VertxMqttServer server = new VertxMqttServer(properties.getId());
        initServer(server, properties);
        return server;
    }

    private void initServer(VertxMqttServer server, VertxMqttServerProperties properties) {
        List<MqttServer> instances = Lists.newArrayListWithCapacity(properties.getInstance());
        for (int i = 0; i < properties.getInstance(); i++) {
            // 创建vertx的mqttServer实例
            MqttServer mqttServer = MqttServer.create(vertx, properties.getOptions());
            instances.add(mqttServer);
        }
        server.setMqttServer(instances);
        for (MqttServer instance : instances) {
            instance.listen(result -> {
                if (result.succeeded()) {
                    log.debug("startup mqtt server [{}] on port :{} ", properties.getId(), result.result().actualPort());
                } else {
                    log.warn("startup mqtt server [{}] error ", properties.getId(), result.cause());
                }
            });
        }
    }


    @Override
    public void reload(Network network, VertxMqttServerProperties properties) {

    }

    @Override
    public ConfigMetadata getConfigMetadata() {
        return null;
    }

    @Override
    public Mono<VertxMqttServerProperties> createConfig(NetworkProperties properties) {
        return null;
    }
}
