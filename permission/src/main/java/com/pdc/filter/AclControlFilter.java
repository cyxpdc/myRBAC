package com.pdc.filter;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pdc.common.ApplicationContextHelper;
import com.pdc.common.JsonData;
import com.pdc.common.RequestHolder;
import com.pdc.model.SysUser;
import com.pdc.service.SysCoreService;
import com.pdc.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限拦截
 * 配置拦截的url需要小于等于loginFilter
 * 否则可能出现某url没有登录，一直无法通过此拦截器的情况
 */
@Slf4j
public class AclControlFilter implements Filter {

    /**
     * 要过滤的url的set集合
     * 使用ConcurrentHashSet达到线程安全的目的
     */
    private static Set<String> exclusionUrlSet = Sets.newConcurrentHashSet();

    /**
     * 如果无权限访问，则跳转到此页面
     */
    private final static String noAuthUrl = "/sys/user/noAuth.page";

    /**
     * 白名单
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String exclusionUrls = filterConfig.getInitParameter("exclusionUrls");
        List<String> exclusionUrlList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(exclusionUrls);
        exclusionUrlSet = Sets.newConcurrentHashSet(exclusionUrlList);
        //noAuth.page必须可以访问，否则陷入死循环，不停跳转noAuthUrl
        exclusionUrlSet.add(noAuthUrl);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String servletPath = request.getServletPath();
        Map requestMap = request.getParameterMap();
        //如果为白名单，则不用拦截
        if (exclusionUrlSet.contains(servletPath)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        SysUser curUser = RequestHolder.getCurrentUser();
        if (curUser == null) {
            log.info("someone visit {}, but no login, parameter:{}", servletPath, JsonMapper.obj2String(requestMap));
            noAuth(request, response);
            return;
        }
        //因为filter不是Spring管理的，所以SysCoreService需要通过popBean取出来
        SysCoreService sysCoreService = ApplicationContextHelper.getBean(SysCoreService.class);
        if (!sysCoreService.hasUrlAcl(servletPath)) {
            log.info("{} visit {}, but no login, parameter:{}", JsonMapper.obj2String(curUser), servletPath, JsonMapper.obj2String(requestMap));
            noAuth(request, response);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
        return;
    }

    /**
     * 没有权限进行操作的时候，调用此方法
     * @param request
     * @param response
     * @throws IOException
     */
    private void noAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletPath = request.getServletPath();
        if (servletPath.endsWith(".json")) {
            JsonData jsonData = JsonData.fail("没有访问权限，如需要访问，请联系管理员");
            response.setHeader("Content-Type", "application/json");
            response.getWriter().print(JsonMapper.obj2String(jsonData));
        } else {//.page
            clientRedirect(noAuthUrl, response);
        }
    }

    private void clientRedirect(String url, HttpServletResponse response) throws IOException{
        response.setHeader("Content-Type", "text/html");
        //输出一个页面，这个页面会进行跳转
        response.getWriter().print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "<head>\n" + "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n"
                + "<title>跳转中...</title>\n" + "</head>\n" + "<body>\n" + "跳转中，请稍候...\n" + "<script type=\"text/javascript\">//<![CDATA[\n"
                + "window.location.href='" + url + "?ret='+encodeURIComponent(window.location.href);\n" + "//]]></script>\n" + "</body>\n" + "</html>\n");
    }

    @Override
    public void destroy() {

    }
}
