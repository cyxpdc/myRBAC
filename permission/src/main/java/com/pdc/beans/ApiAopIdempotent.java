package com.pdc.beans;

import com.pdc.util.RedisTokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author PDC
 */
@Aspect
@Component
public class ApiAopIdempotent {

    @Autowired
    private RedisTokenUtils redisTokenUtils;

    @Pointcut("execution(public * com.pdc.controller.*.*(..))")
    public void rlAop() {
    }

    // 前置通知：转发Token参数
    @Before("rlAop()")
    public void before(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        ApiToken apiToken = signature.getMethod().getDeclaredAnnotation(ApiToken.class);
        if (apiToken != null) {
            apiToken();//将token放入request头
        }
    }

    // 环绕通知：验证Token
    @Around("rlAop()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        ApiIdempotent apiIdempotent = signature.getMethod().getDeclaredAnnotation(ApiIdempotent.class);
        if (apiIdempotent != null) {
            return apiIdempotent(proceedingJoinPoint, signature);
        }
        // 放行
        return proceedingJoinPoint.proceed();
    }

    /**
     * 验证Token
     * @param proceedingJoinPoint
     * @param signature
     * @return
     * @throws Throwable
     */
    public Object apiIdempotent(ProceedingJoinPoint proceedingJoinPoint, MethodSignature signature)  throws Throwable {
        ApiIdempotent apiIdempotent = signature.getMethod().getDeclaredAnnotation(ApiIdempotent.class);
        if (apiIdempotent == null) {
            // 直接执行程序
            return proceedingJoinPoint.proceed();
        }
        // 代码步骤：
        // 1.获取令牌 存放在请求头或隐藏域中
        // 2.判断令牌是否在缓存中有对应的令牌
        // 3.如何缓存没有该令牌的话，直接报错（请勿重复提交）
        // 4.如何缓存有该令牌的话，则删除该令牌，可以执行业务逻辑
        HttpServletRequest request = getRequest();
        String valueType = apiIdempotent.type();
        if (StringUtils.isEmpty(valueType)) {
            response("参数错误!");
            return null;
        }
        String token = null;
        if (valueType.equals(ConstantUtils.EXTAPIHEAD)) {
            token = request.getHeader("token");
        } else {
            token = request.getParameter("token");
        }
        if (StringUtils.isEmpty(token)) {
            response("参数错误!");
            return null;
        }
        if (!redisTokenUtils.findToken(token)) {
            response("请勿重复提交!");
            return null;
        }
        return proceedingJoinPoint.proceed();
    }
    //jsp页面通过${token}放入隐藏域中
    public void apiToken() {
        getRequest().setAttribute("token", redisTokenUtils.getToken());
    }

    public HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        return request;
    }

    public void response(String msg) throws IOException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = attributes.getResponse();
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        try {
            writer.println(msg);
        } catch (Exception e) {

        } finally {
            writer.close();
        }
    }
}