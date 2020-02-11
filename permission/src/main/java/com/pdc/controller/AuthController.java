package com.pdc.controller;

import com.alibaba.fastjson.JSONObject;
import com.pdc.beans.ResponseBase;
import com.pdc.dao.AppMapper;
import com.pdc.model.AppEntity;
import com.pdc.service.BaseApiService;
import com.pdc.service.RedisPool;
import com.pdc.util.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建、获取getAccessToken
 * @author PDC
 */
@RestController
@RequestMapping(value = "/auth")
public class AuthController{

    @Autowired
    private RedisPool redisPool;
    private static final int timeToken = 60 * 60 * 2;
    @Autowired
    private AppMapper appMapper;

    // 使用appId+appSecret 生成AccessToke
    @RequestMapping("/getAccessToken")
    public ResponseBase getAccessToken(AppEntity appEntity) {
        AppEntity appResult = appMapper.findByApp(appEntity);
        if (appResult == null) {
            return BaseApiService.setResultError("没有对应机构的认证信息");
        }
        int isFlag = appResult.getIsFlag();
        if (isFlag == 1) {
            return BaseApiService.setResultError("暂时对该机构不开放");
        }
        // 删除旧的accessToken、生成新的accessToken存入数据库和缓存中
        String newAccessToken = processAccessToken(appResult.getAppId(),appResult.getAppSecret(),appResult.getAccessToken());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessToken", newAccessToken);
        return BaseApiService.setResultSuccessData(jsonObject);
    }

    public String processAccessToken(String appId, String appSecret, String preAccessToken) {
        //在获取新的accessToken之前删除之前老的accessToken
        if(!StringUtils.isEmpty(preAccessToken)){//第一次调用的情况下为null
            redisPool.instance().del(preAccessToken);
        }
        //使用appid+appsecret 生成对应的AccessToken 保存两个小时
        //可以写一个定时job，每隔1小时50分后刷入最新的AccessToken即可
        String accessToken = TokenUtils.getAccessToken(appId,appSecret);
        // 保证在同一个事务中
        // 生成最新的token key为accessToken value为appid
        redisPool.instance().setex(accessToken,timeToken,appId);//accessToken建议加密后再存储
        // 保存当前的accessToken
        appMapper.updateAccessToken(accessToken, appId);
        return accessToken;
    }
}
