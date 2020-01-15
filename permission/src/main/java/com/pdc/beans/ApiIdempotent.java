package com.pdc.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//解决接口幂等性问题的注解
//需要支持网络延迟和表单重复提交两种原因
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiIdempotent {
    String type();//用来表示token来自请求头还是RPC
}
