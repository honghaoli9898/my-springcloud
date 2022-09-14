package com.seaboxdata.sdps.licenseclient.service.filter;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

//这个filter的执行比GoffFilterSecurityInterceptor后，所以如果想调整可以尝试@Order看是否能达到你要的效果，
//或者干脆把filter就都放一个filter里，不使用多个filter
@Component
@ServletComponentScan
@WebFilter(urlPatterns = "/", filterName = "AuthAccessFilter")
public class AuthFilterConfiguration implements Filter{

    @Override
    public void destroy() {
    }

    /**
     * 过滤器，可以根据你的路径规划--对某些路径进行鉴权处理或通过处理, 也可以定义多个filter来处理
     * HttpServletResponse response
     * HttpServletRequest request
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        //判断是否有鉴权
//		if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()){}
        HttpServletRequest req = (HttpServletRequest) request;
//		if (req.getHeader("your-key") == null) {
//			response.setContentType("text/json;charset=utf-8");
//			request.setCharacterEncoding("UTF-8");
//			Writer writer = response.getWriter();
//			try {
//				writer.write("your response content");
//				writer.flush();
//				writer.close();
//			} catch (IOException e2) {
//
//			}
//			return;
//		}
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
