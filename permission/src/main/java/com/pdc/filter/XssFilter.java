package com.pdc.filter;


import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * @author PDC
 */
public class XssFilter implements Filter{

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        XssHttpServletRequestWrapper request = new XssHttpServletRequestWrapper((HttpServletRequest) servletRequest);
        filterChain.doFilter(request, servletResponse);
    }

    class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private HttpServletRequest request;

        /**
         * @param request
         */
        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        @Override
        public String getParameter(String name) {
            // 过滤getParameter参数 检查是否有特殊字符
            String value = super.getParameter(name);
            System.out.println("value:" + value);
            if (!StringUtils.isEmpty(value)) {
                // 将中文转换为字符编码格式，将特殊字符变为html源代码保存
                value = StringEscapeUtils.escapeHtml4(value);
                System.out.println("newValue:" + value);
            }
            return value;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }
    @Override
    public void destroy() { }
}
