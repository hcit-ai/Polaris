package com.polaris.gateway.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.polaris.gateway.GatewayConstant;
import com.polaris.gateway.util.ConfUtil;
import com.polaris.comm.util.LogUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author:Winning
 *
 * Description:
 *
 * URL路径黑名单拦截
 */
@Service
public class UrlHttpRequestFilter extends HttpRequestFilter {
	private static LogUtil logger = LogUtil.getInstance(UrlHttpRequestFilter.class);

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
            for (Pattern pat : ConfUtil.getPattern(FilterType.URL.name())) {
                Matcher matcher = pat.matcher(url);
                if (matcher.find()) {
                    hackLog(logger, GatewayConstant.getRealIp(httpRequest, channelHandlerContext), FilterType.URL.name(), pat.toString());
                    return true;
                }
            }
        }
        return false;
    }
}
