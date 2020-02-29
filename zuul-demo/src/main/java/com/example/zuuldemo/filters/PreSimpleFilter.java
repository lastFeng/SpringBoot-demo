package com.example.zuuldemo.filters;

import com.apple.eawt.AppEvent;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author guowf
 * @mail: guowf_buaa@163.com
 * @date created in 2020/2/27 09:33
 * @description:
 */
public class PreSimpleFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(PreSimpleFilter.class);

    /**
     * 过滤器的类型
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 给出过滤器的执行顺序，相对其他过滤器的顺序
     * @return
     */
    @Override
    public int filterOrder() {
        return 1;
    }

    /**
     * 在哪些情况下，会执行过滤操作
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤的执行动作
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        logger.info("Hello Pre");

        return null;
    }
}
