package org.tlinks.network.mqtt;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * @author : zzh
 * create at:  2022/9/2
 * @description:
 */
public interface MqttConnection {

    /**
     * 获取MQTT客户端ID
     *
     * @return clientId
     */
    String getClientId();

    /**
     * 获取MQTT认证信息
     *
     * @return 可选的MQTT认证信息
     */
    //Optional<MqttAuth> getAuth();

    /**
     * 拒绝MQTT连接
     *
     * @param code 返回码
     * @see MqttConnectReturnCode
     */
    void reject(MqttConnectReturnCode code);

    /**
     * 接受连接.接受连接后才能进行消息收发.
     *
     * @return 当前连接信息
     */
    MqttConnection accept();

    /**
     * 关闭mqtt连接
     *
     * @return 异步关闭结果
     */
    Mono<Void> close();

    /**
     * 监听断开连接
     *
     * @param listener 监听器
     */
    void onClose(Consumer<MqttConnection> listener);

    /**
     * 推送消息到客户端
     *
     * @param message MQTT消息
     * @return 异步推送结果
     */
    Mono<Void> publish(MqttMessage message);


    /**
     * 订阅客户端推送的消息
     *
     * @return 消息流
     */
    Flux<MqttMessage> handleMessage();

    /**
     * 订阅客户端订阅请求
     *
     * @param autoAck 是否自动应答
     * @return 订阅请求流
     */
    Flux<MqttSubscription> handleSubscribe(boolean autoAck);

    /**
     * 订阅客户端取消订阅请求
     *
     * @param autoAck 是否自动应答
     * @return 取消订阅请求流
     */
    Flux<MqttUnSubscription> handleUnSubscribe(boolean autoAck);

}
