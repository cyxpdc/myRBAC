package com.mmall.beans;

import lombok.Getter;

@Getter
public enum CacheKeyConstants {

    SYSTEM_ACLS,
    /**
     * 需要绑定用户id
     */
    USER_ACLS;

}
