package org.tlinks.network.mqtt;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

/**
 * @author : zzh
 * create at:  2022/9/13
 * @description:
 */
public interface EncodeMessage {

    /**
     * 获取原始报文
     *
     * @return ByteBuf
     */
    @Nonnull
    ByteBuf getPayload();


    default String payloadAsString() {
        return getPayload().toString(StandardCharsets.UTF_8);
    }


}
