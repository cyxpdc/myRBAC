package com.pdc.util;

import com.pdc.service.RedisPool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author PDC
 */
@Component
public class RedisTokenUtils {

    @Resource(name = "redisPool")
    private RedisPool redisPool;

    private static final int timeout = 60 * 60;

    // 生成一个token并将其存入Redis中
    public String getToken() {
        String token = UUID.randomUUID().toString().replace("-", "") + "token";
        ShardedJedis shardedJedis = redisPool.instance();
        shardedJedis.setex(token, timeout,token);
        return token;
    }

    public boolean findToken(String tokenKey) {
        ShardedJedis shardedJedis = redisPool.instance();
        String token = shardedJedis.get(tokenKey);
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        //token获取成功后需要删除对应tokenMapstoken
        shardedJedis.del(token);
        return true;
    }
}
