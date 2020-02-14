package com.pdc.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author PDC
 */
public class ImgFilter implements Filter {

    @Value("可自定义")
    private String domainName;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String referer = req.getHeader("Referer");
        if (StringUtils.isEmpty(referer)) {//png可自定义
            servletRequest.getRequestDispatcher("/imgs/pdc .png").forward(servletRequest, servletResponse);
            return;
        }
        String domain = getDomain(referer);
        if (!domain.equals(domainName)) {//png可自定义
            servletRequest.getRequestDispatcher("/imgs/pdc.png").forward(servletRequest, servletResponse);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 如https://www.cnblogs.com/liurwei/p/9572136.html，会截到www.cnblogs.com
     * @param url
     * @return
     */
    public String getDomain(String url) {
        String result = "";
        int j = 0, startIndex = 0, endIndex = 0;
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '/') {
                j++;
                if (j == 2)
                    startIndex = i + 1;
                else if (j == 3)
                    endIndex = i;
            }
        }
        result = url.substring(startIndex, endIndex);
        return result;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
