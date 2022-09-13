package org.tlinks.network.mqtt.server.vertx;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.tlinks.network.mqtt.MqttConnection;
import org.tlinks.network.mqtt.server.MqttServer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Collection;

/**
 * @author : zzh
 * create at:  2022/9/2
 * @description:
 */
@Slf4j
public class VertxMqttServer implements MqttServer {

    private Collection<io.vertx.mqtt.MqttServer> mqttServer;

    private final Sinks.Many<MqttConnection> dispatcher = Sinks.many().replay().all();

    private final String id;

    public VertxMqttServer(String id) {
        this.id = id;
    }

    public void setMqttServer(Collection<io.vertx.mqtt.MqttServer> mqttServer) {
        if (this.mqttServer != null && !this.mqttServer.isEmpty()) {
            shutdown();
        }
        this.mqttServer = mqttServer;
        for (io.vertx.mqtt.MqttServer server : this.mqttServer) {
            server
                .exceptionHandler(error -> {
                    log.error(error.getMessage(), error);
                })
                .endpointHandler(endpoint -> {
                    if (dispatcher.currentSubscriberCount() == 0) {
                        log.info("mqtt server no handler for:[{}]", endpoint.clientIdentifier());
                        endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
                        return;
                    }
                    // TODO 超时控制

                    dispatcher.tryEmitNext(new VertxMqttConnection(endpoint));
                });
        }
    }


    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void shutdown() {
        if (mqttServer != null) {
            for (io.vertx.mqtt.MqttServer server : mqttServer) {
                server.close(res -> {
                    if (res.failed()) {
                        log.error(res.cause().getMessage(), res.cause());
                    } else {
                        log.debug("mqtt server [{}] closed", server.actualPort());
                    }
                });
            }
            mqttServer.clear();
        }
    }

    @Override
    public Flux<MqttConnection> acquireConnection() {
        return dispatcher.asFlux();
    }
}
