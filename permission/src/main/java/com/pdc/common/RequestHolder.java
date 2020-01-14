package com.pdc.common;

import com.pdc.model.SysUser;

import javax.servlet.http.HttpServletRequest;

/**
 * @author pdc
 */
public class RequestHolder {
    /**
     * 保存每一个线程的用户信息
     */
    private static final ThreadLocal<SysUser> userHolder = new ThreadLocal<>();

    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

    public static void addCurrentUser(SysUser sysUser) {
        userHolder.set(sysUser);
    }

    public static void addCurrentRequest(HttpServletRequest request) {
        requestHolder.set(request);
    }

    public static SysUser getCurrentUser() {
        return userHolder.get();
    }

    public static HttpServletRequest getCurrentRequest() {
        return requestHolder.get();
    }

    public static void remove() {
        userHolder.remove();
        requestHolder.remove();
    }
}
