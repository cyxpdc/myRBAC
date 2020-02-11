package com.pdc.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 机构注册的实体
 * @author PDC
 */
@Getter
@Setter
public class AppEntity {

    private long id;

    private String appId;

    private String appName;

    private String appSecret;

    private String accessToken;

    private int isFlag;
}
