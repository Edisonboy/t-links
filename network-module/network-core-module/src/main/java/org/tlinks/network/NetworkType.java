package org.tlinks.network;

import java.util.List;
import java.util.Optional;

/**
 *
 * 网络组件类型，通常使用枚举实现
 *
 * @author : zzh
 * create at:  2022/9/6
 */
public interface NetworkType {

    /**
     * 类型唯一标识
     *
     * @return
     */
    String getId();

    /**
     * 类型名称
     *
     * @return
     */
    default String getName() {
        return getId();
    }

    /**
     * 使用指定的ID创建一个NetworkType
     *
     * @param id ID
     * @return NetworkType
     */
    static NetworkType of(String id) {
        return () -> id;
    }

    /**
     * 获取所有支持的网络组件类型
     *
     * @return 所有支持的网络组件类型
     */
    static List<NetworkType> getAll() {
        return NetworkTypes.get();
    }

    /**
     * 根据网络组件类型ID获取类型对象
     *
     * @param id ID
     * @return Optional
     */
    static Optional<NetworkType> lookup(String id) {
        return NetworkTypes.lookup(id);
    }
}
