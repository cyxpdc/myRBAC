package com.pdc.filter;

import com.alibaba.fastjson.JSONObject;
import com.pdc.dao.AppMapper;
import com.pdc.model.AppEntity;
import com.pdc.service.BaseApiService;
import com.pdc.service.RedisPool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 验证AccessToken 是否正确
 * 配置拦截的地址可以为“/openApi/”，即对外开放的接口Controller
 * @author PDC
 */
public class AccessTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisPool redisPool;
    @Autowired
    private AppMapper appMapper;

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        System.out.println("---------------------开始进入请求地址拦截----------------------------");
        String accessToken = httpServletRequest.getParameter("accessToken");
        if (StringUtils.isEmpty(accessToken)) {
            resultError("参数Token为null", httpServletResponse);
            return false;
        }
        String appId = redisPool.instance().get(accessToken);
        if (StringUtils.isEmpty(appId)) {
            resultError("accessToken 已经失效", httpServletResponse);
            return false;
        }
        AppEntity appResult = appMapper.findAppId(appId);
        if(appResult == null){
            resultError("没有找到对应的app消息", httpServletResponse);
            return false;
        }
        int isFlag = appResult.getIsFlag();
        if(isFlag == 1){
            resultError("暂时没有权限", httpServletResponse);
            return false;
        }
        // 正常执行业务逻辑
        return true;

    }

    // 返回错误提示
    public void resultError(String errorMsg, HttpServletResponse httpServletResponse) {
        PrintWriter printWriter = null;
        try {
            printWriter = httpServletResponse.getWriter();
            printWriter.write(new JSONObject().toJSONString(BaseApiService.setResultError(errorMsg)));
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        System.out.println("--------------处理请求完成后视图渲染之前的处理操作---------------");
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,Object o, Exception e) throws Exception {
        System.out.println("---------------视图渲染之后的操作-------------------------0");
    }
}
