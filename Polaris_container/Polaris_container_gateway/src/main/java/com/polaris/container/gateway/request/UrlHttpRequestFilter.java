package com.polaris.container.gateway.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.container.gateway.GatewayConstant;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author:Tom.Yu
 *
 * Description:
 *
 * URL路径黑名单拦截
 */
public class UrlHttpRequestFilter extends HttpRequestFilter {
	private static Logger logger = LoggerFactory.getLogger(UrlHttpRequestFilter.class);

    @Override
    public boolean doFilter(HttpRequest originalRequest, HttpObject httpObject, ChannelHandlerContext channelHandlerContext) {
        if (httpObject instanceof HttpRequest) {
            logger.debug("filter:{}", this.getClass().getName());
            HttpRequest httpRequest = (HttpRequest) httpObject;
            String url;
            int index = httpRequest.uri().indexOf("?");
            if (index > -1) {
                url = httpRequest.uri().substring(0, index);
            } else {
                url = httpRequest.uri();
            }
            for (Pattern pat : FilterTypeHelper.getPattern(FilterType.URL.name())) {
                Matcher matcher = pat.matcher(url);
                if (matcher.find()) {
                    hackLog(logger, GatewayConstant.getRealIp(httpRequest), FilterType.URL.name(), pat.toString());
                    return true;
                }
            }
        }
        return false;
    }
}
