package com.pdc.common;

import com.pdc.exception.ParamException;
import com.pdc.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理器
 * 需要配置在spring-servlet中，成为bean
 * @author pdc
 */
@Slf4j
public class SpringExceptionResolver implements HandlerExceptionResolver {
    /**
     * 最终得到的ModelAndView会返回给response
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @return
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView modelAndView;
        String defaultMsg = "System error";

        String url = request.getRequestURL().toString();
        //这里我们要求项目中所有请求json数据，都使用.json结尾
        if (url.endsWith(".json")) {
            if (ex instanceof PermissionException || ex instanceof ParamException) {
                JsonData result = JsonData.fail(ex.getMessage());
                //result.toHashMap()使异常返回结果跟正常返回结果一致，达到良好的用户体验
                //因为正常返回结果使用了@ResponseBody转为Json，也相当于hashmap了
                //jsonView则为spring-servlet里定义的bean,相当于Json返回
                //那么这里也就相当于将Hashmap的数据解析为json，送到页面
                modelAndView = new ModelAndView("jsonView", result.toHashMap());
            } else {
                log.error("unknown json exception, url:" + url, ex);
                JsonData result = JsonData.fail(defaultMsg);
                modelAndView = new ModelAndView("jsonView", result.toHashMap());
            }
        }
        //这里我们要求项目中所有请求page页面，都使用.page结尾
        //如果是页面出错，则返回exception.jsp
        else if (url.endsWith(".page")){
            log.error("unknown page exception, url:" + url, ex);
            JsonData result = JsonData.fail(defaultMsg);
            modelAndView = new ModelAndView("exception", result.toHashMap());
        }
        else {
            log.error("unknow exception, url:" + url, ex);
            JsonData result = JsonData.fail(defaultMsg);
            modelAndView = new ModelAndView("jsonView", result.toHashMap());
        }
        return modelAndView;
    }
}
