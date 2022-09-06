package org.tlinks.network;

/**
 * @author : zzh
 * create at:  2022/9/2
 * @description:
 */
public interface Network {

    /**
     * ID唯一标识
     *
     * @return ID
     */
    String getId();

    /**
     * 关闭网络组件
     */
    void shutdown();


}
