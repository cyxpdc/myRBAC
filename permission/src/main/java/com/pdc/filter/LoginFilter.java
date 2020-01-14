package com.pdc.filter;

import com.pdc.common.RequestHolder;
import com.pdc.model.SysUser;
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

/**
 * 判断用户是否登录
 */
@Slf4j
public class LoginFilter implements Filter {
    /**
     * 需要配置在web.xml中
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        SysUser sysUser = (SysUser)request.getSession().getAttribute("user");
        if (sysUser == null) {
            //bug：这里要加/，否则提示：localhost将您重定向的次数过多
            //如/admin/index.page,没/的话，会变成admin/signin.jsp
            //无法跳转到登录页面/signin.jsp，所以需要加上/变成绝对路径
            String path = "/signin.jsp";
            response.sendRedirect(path);
            return;
        }
        RequestHolder.addCurrentUser(sysUser);
        RequestHolder.addCurrentRequest(request);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
    }

    public void init(FilterConfig filterConfig)  { }

    public void destroy() { }
}
