package com.pdc.beans;

public final class BaseApiConstants {
    // 响应请求成功
    public static final String HTTP_RES_CODE_200_VALUE = "success";
    // 系统错误
    public static final String HTTP_RES_CODE_500_VALUE = "fial";
    // 响应请求成功code
    public static final Integer HTTP_RES_CODE_200 = 200;
    // 系统错误
    public static final Integer HTTP_RES_CODE_500 = 500;
    // 未关联QQ账号
    public static final Integer HTTP_RES_CODE_201 = 201;
    // 发送邮件
    public static final String MSG_EMAIL = "email";
    // 会员token
    public static final String TOKEN_MEMBER = "TOKEN_MEMBER";
    // 支付token
    public static final String TOKEN_PAY = "TOKEN_pay";
    // 支付成功
    public static final String PAY_SUCCESS = "success";
    // 支付失败
    public static final String PAY_FAIL = "fail";
    // 用户有效期 90天
    public static final Long TOKEN_MEMBER_TIME = (long) (60 * 60 * 24 * 90);
    public static final int COOKIE_TOKEN_MEMBER_TIME = (60 * 60 * 24 * 90);
    public static final Long PAY_TOKEN_MEMBER_TIME = (long) (60 * 15);
    // cookie 会员 totoken 名称
    public static final String COOKIE_MEMBER_TOKEN = "cookie_member_token";
}
