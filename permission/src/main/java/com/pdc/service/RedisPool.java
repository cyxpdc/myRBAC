package com.pdc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisPool {
    /**
     * 处理redis的单例service
     */
    @Resource(name = "shardedJedisPool")
    private ShardedJedisPool shardedJedisPool;

    /**
     * 获取实例
     * @return
     */
    public ShardedJedis instance() {
        return shardedJedisPool.getResource();
    }

    /**
     * 关闭连接
     * @param shardedJedis
     */
    public void safeClose(ShardedJedis shardedJedis) {
       if (shardedJedis != null) {
           shardedJedis.close();
       }
    }
}
